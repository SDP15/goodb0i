package com.sdp15.goodb0i.data.navigation.sockets

import androidx.collection.CircularArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.Route
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.RetrofitProvider
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.data.store.products.ProductLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max

/**
 * Manages state across a shopping session
 * I feel like this could easily turn into a God-Object which deals with far too much information. It may need
 * decomposing later
 */
class WebSocketShoppingSession(

    private val sh: SocketHandler<Message.IncomingMessage, Message.OutgoingMessage>,
    private val productLoader: ProductLoader
) : ShoppingSession {

    private var uid: String = "" // Server session id
    private var route: Route = Route.emptyRoute()
    private var index = 0 // Index within the route
    private val lastPointSeen: Route.RoutePoint // Last point received from trolley
        get() = route[index]
    private var lastStopLocation: Route.RoutePoint.IndexPoint =
        Route.RoutePoint.IndexPoint.IdentifiedPoint.Start(0, "") // Last rack we stopped at
    private val nextStopPoint: Route.RoutePoint.IndexPoint // Either a shelf to stop at, or the end
        get() = route.subList(fromIndex = index + 1, toIndex = route.size)
            .firstOrNull { point -> point is Route.RoutePoint.IndexPoint.IdentifiedPoint.Stop ||
                    point is Route.RoutePoint.IndexPoint.IdentifiedPoint.End
            } as Route.RoutePoint.IndexPoint // this can't fail so long as the route is well formed (contains "end")

    private var shoppingList: ShoppingList = ShoppingList.emptyList()
    private var lastScannedProduct: Product? = null
    private val collectedProducts = ArrayList<ListItem>()

    // Products to collect from the current rack
    private val remainingRackProducts: MutableList<ListItem> = mutableListOf()

    // In the event of disconnection we have to restore the previous state after exiting the disconnected state
    private val movingStates = CircularArray<ShoppingSessionState>(10)
    private fun setState(state: ShoppingSessionState) {
        Timber.i("Switching to state $state")
        sessionState.postValue(state)
        if (state is ShoppingSessionState.NavigatingTo ||
            state is ShoppingSessionState.Scanning ||
            state is ShoppingSessionState.Confirming ||
            state is ShoppingSessionState.Checkout) {
            movingStates.addLast(state)
        }
    }

    private val sessionState = MutableLiveData<ShoppingSessionState>().apply {
        postValue(ShoppingSessionState.NoSession)
    }

    override val state: LiveData<ShoppingSessionState> = sessionState

    init {
        //TODO: Handle disconnecting observers
        sh.connectionState.observeForever { state ->
            if (state == SocketHandler.SocketState.ErrorDisconnect) {
                Timber.i("Socket state disconnected. Attempting reconnect")
                setState(ShoppingSessionState.Disconnected)
                attemptReconnection()
            }
        }
        sh.incomingMessages.observeForever { message ->
            Timber.i("$message")
            when (message) {
                is Message.IncomingMessage.Connected -> {
                    uid = message.id
                    setState(ShoppingSessionState.NegotiatingTrolley)
                }
                is Message.IncomingMessage.NoAvailableTrolley -> {
                    setState(ShoppingSessionState.NoSession)
                }
                is Message.IncomingMessage.TrolleyConnected -> {
                    setState(ShoppingSessionState.Connected)
                }
                is Message.IncomingMessage.UserReady -> {
                    postMovingState() // First moving state
                }
                is Message.IncomingMessage.RouteCalculated -> {
                    route = message.route
                    sh.sendMessage(Message.OutgoingMessage.ReceivedRoute)
                }
                is Message.IncomingMessage.ReachedPoint -> {
                    reachedPoint(message.id)
                }
                is Message.IncomingMessage.TrolleyAcceptedProduct -> {
                    if (state.value is ShoppingSessionState.Confirming) productAcceptedInternal()
                }
                is Message.IncomingMessage.TrolleyRejectedProduct -> {
                    if (state.value is ShoppingSessionState.Confirming) productRejectedInternal()
                }
                is Message.IncomingMessage.TrolleySkippedProduct -> {
                    if (state.value is ShoppingSessionState.Scanning) skipProductInternal()
                }
                is Message.IncomingMessage.Replan -> {
                    route.insertSubRoute(lastPointSeen, message.subRoute)
                    lastStopLocation = message.subRoute.first() as Route.RoutePoint.IndexPoint
                    val remaining = route.subList(max(0, index-1), route.size)
                    val pointIndex = max(0, index-1) + remaining.indexOfFirst { rp -> rp is Route.RoutePoint.IndexPoint.IdentifiedPoint && rp.id == (lastStopLocation as Route.RoutePoint.IndexPoint.IdentifiedPoint).id     }
                    index = pointIndex
//                    lastStopLocation = message.subRoute.first() as Route.RoutePoint.IndexPoint
//                    index += route.subList(index, route.size).indexOfFirst { point ->
//                        (point as Route.RoutePoint.IndexPoint.IdentifiedPoint).id ==
//                                (lastStopLocation)
//                    }
                    if (state.value is ShoppingSessionState.NavigatingTo) {
                        postMovingState()
                    }
                }
            }
        }
    }

    override fun startSession(list: ShoppingList) {
        if (!sh.isConnected) {
            Timber.i("Starting session $list")
            shoppingList = list
            setState(ShoppingSessionState.Connecting)
            sh.start(RetrofitProvider.root + "/app")
            sh.sendMessage(Message.OutgoingMessage.PlanRoute(list.code))
        }
    }

    override fun endSession() {
        uid = ""
        sh.sendMessage(Message.OutgoingMessage.SessionComplete)
        sh.stop()
        // We expect to be destroyed immediately, so this might not be necessary
        sessionState.postValue(ShoppingSessionState.NoSession)
    }


    /*

     */
    private fun reachedPoint(id: String) {
        Timber.i("Searching for $id")
        val remaining = route.subList(max(0, index-1), route.size)
        val pointIndex = max(0, index-1) + remaining.indexOfFirst { rp -> rp is Route.RoutePoint.IndexPoint.IdentifiedPoint && rp.id == id }
        val point = route.getOrNull(pointIndex)
        Timber.i("Found $point at $pointIndex")
        if (point is Route.RoutePoint.IndexPoint.IdentifiedPoint.Stop) {
            index = pointIndex
            Timber.i("At stop at $id")
            lastStopLocation = point
            setState(ShoppingSessionState.Scanning(point, remainingRackProducts))
        } else if (point is Route.RoutePoint.IndexPoint.IdentifiedPoint.Pass) {
            // I haven't seen this happen, but we REALLY don't want to change to a navigating state
            // while we are in the process of scanning
            if (sessionState.value is ShoppingSessionState.NavigatingTo) {
                index = pointIndex
                // We should already be in a NavigatingTo state at this point, because we only switch to
                // NavigatingTo once a shelf rack has been completed
                setState(
                    ShoppingSessionState.NavigatingTo(
                        from = lastStopLocation, to = nextStopPoint, at = point, products = remainingRackProducts
                    )
                )
            }
        } else if (point is Route.RoutePoint.IndexPoint.IdentifiedPoint.End) {
            setState(ShoppingSessionState.Checkout(shoppingList, collectedProducts))
        } else {
            Timber.e("Unknown point $id at index $pointIndex")
        }
    }

    /*
        Post a state moving to the next point at which we need to stop
     */
    private fun postMovingState() {
        Timber.i("Moving from ${route[index]} to ${route[index + 1]}")
        val point = nextStopPoint
        Timber.i("Found next stop at at $point")
        if (point is Route.RoutePoint.IndexPoint.IdentifiedPoint.Stop) {
            // Post the upcoming products for
            val indices = point.productIndices
            Timber.i("Posting upcoming products with indices $indices")
            remainingRackProducts.clear()
            remainingRackProducts.addAll(shoppingList.products.slice(indices))
            setState(
                ShoppingSessionState.NavigatingTo(
                    from = lastStopLocation, to = point, at = lastPointSeen as Route.RoutePoint.IndexPoint,
                    products = remainingRackProducts
                )
            )
        } else if (point is Route.RoutePoint.IndexPoint.IdentifiedPoint.End) {
            remainingRackProducts.clear()
            setState(
                ShoppingSessionState.NavigatingTo(
                    from = lastStopLocation, at = lastPointSeen as Route.RoutePoint.IndexPoint, to = point,
                    products = remainingRackProducts
                )
            )
        }
    }

    /*
     Attempt to match a scanned barcode to a products
     As we are on a local network, making a GET request is currently not an issue
     TODO: Check against cached products for the current shelf
     */
    override suspend fun checkScannedCode(code: String): Product? {
        var product = shoppingList.products.firstOrNull { item -> item.product.gtin == code }?.product
        if (product == null) {
            val fromServer = productLoader.searchBarcode(code)
            if (fromServer is Result.Success) {
                product = fromServer.data
            }
        }
        if (product != null) {
            lastScannedProduct = product
            sh.sendMessage(Message.OutgoingMessage.ProductScanned(product.id))
            setState(ShoppingSessionState.Confirming(product))
        }
        return product
    }

    override fun skipProduct() {
        sh.sendMessage(Message.OutgoingMessage.SkippedProduct)
        skipProductInternal()
    }

    private fun skipProductInternal() {
        remainingRackProducts.removeAt(0)
        switchToNextListItem()
    }

    override fun productAccepted() {
        sh.sendMessage(Message.OutgoingMessage.AcceptedProduct(lastScannedProduct!!.id))
        productAcceptedInternal()
    }

    /*
     Change session state when products is accepted
     Either by the app or via message from trolley
     */
    private fun productAcceptedInternal() {

        val accepted = lastScannedProduct!!
        val count = collectedProducts.indexOfFirst { item -> item.product == accepted }
        if (count == -1) {
            collectedProducts += ListItem(accepted)
        } else {
            collectedProducts[count] = collectedProducts[count] + 1
        }

        val current = remainingRackProducts.first()
        if (accepted == current.product) {
            // Decrement quantity of current products
            Timber.i("Decrementing quantity for $current")
            remainingRackProducts[0] = current - 1
            if (remainingRackProducts.first().quantity == 0) {
                Timber.i("Removing products with quantity 0")
                remainingRackProducts.removeAt(0)
            }
        }
        switchToNextListItem()
    }

    private fun switchToNextListItem() {

        // If there are no products remaining on the rack, navigate to the next at
        if (remainingRackProducts.isEmpty()) {
            Timber.i("No products remaining for this rack")
            postMovingState()
        } else {
            // Otherwise, we are still in the scanning state
            Timber.i("Products remaining on rack $remainingRackProducts")
            setState(
                ShoppingSessionState.Scanning(
                    lastPointSeen as Route.RoutePoint.IndexPoint.IdentifiedPoint.Stop, remainingRackProducts
                )
            )
        }
    }

    override fun productRejected() {
        sh.sendMessage(Message.OutgoingMessage.RejectedProduct(lastScannedProduct!!.id))
        productRejectedInternal()
    }

    private fun productRejectedInternal() {
        setState(
            ShoppingSessionState.Scanning(
                lastPointSeen as Route.RoutePoint.IndexPoint.IdentifiedPoint.Stop,
                remainingRackProducts
            )
        )
    }

    override fun requestAssistance() {
        sh.sendMessage(Message.OutgoingMessage.RequestHelp)
    }

    // Repeatedly attempt to reconnect to the server
    private var isReconnecting = false
    private fun attemptReconnection() {
        //TODO: How to scope this
        if(isReconnecting) return
        GlobalScope.launch(Dispatchers.IO) {
            //TODO: Break after some number of reconnection attempts
            isReconnecting = true
            while (!sh.isConnected) {
                sh.start(RetrofitProvider.root + "/app")
                sh.sendMessage(Message.OutgoingMessage.Reconnect(uid))
                Thread.sleep(500)
            }
            if(!movingStates.isEmpty) setState(movingStates.last)
            isReconnecting = false
            Timber.i("Socket reconnected")
        }
    }
}