package com.sdp15.goodb0i.data.navigation.sockets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.Route
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.RetrofitProvider
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.data.store.products.ProductLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages state across a shopping session
 * I feel like this could easily turn into a God-Object which deals with far too much information. It may need
 * decomposing later
 */
class SessionManager(

    private val sh: SocketHandler<Message.IncomingMessage, Message.OutgoingMessage>,
    private val productLoader: ProductLoader
) : ShoppingSessionManager<Message.IncomingMessage> {

    private var uid: String = ""
    private var route: Route = Route.emptyRoute()
    private var index = 0
    private var shoppingList: ShoppingList = ShoppingList.emptyList()

    private val incomingMessages = MutableLiveData<Message.IncomingMessage>()
    //TODO: If there isn't a use for publicly exposing direct message access, remove this. (Currently no usages)
    override val incoming: LiveData<Message.IncomingMessage> = incomingMessages

    private val remainingRackProducts: MutableList<ListItem> = mutableListOf()
    private val currentRackProducts = MutableLiveData<List<ListItem>>()
    override val currentProducts: LiveData<List<ListItem>> = currentRackProducts

    private val lastScannedProduct = MutableLiveData<Product>()
    override val scannedProduct: LiveData<Product> = lastScannedProduct

    private val sessionState = MutableLiveData<ShoppingSessionState>().apply {
        postValue(ShoppingSessionState.NoSession)
    }
    override val state: LiveData<ShoppingSessionState> = sessionState

    init {
        //TODO: Handle disconnecting observers
        sh.connectionState.observeForever { state ->
            if (state == SocketHandler.SocketState.ErrorDisconnect) {
                sessionState.postValue(ShoppingSessionState.Disconnected)
                attemptReconnection()
            }
        }
        sh.incomingMessages.observeForever { message ->
            Timber.i("$message")
            var consume = false // We may consume the message and not emit it elsewhere
            when (message) {
                is Message.IncomingMessage.Connected -> {
                    uid = message.id
                    sessionState.postValue(ShoppingSessionState.NegotiatingTrolley)
                }
                is Message.IncomingMessage.NoAvailableTrolley -> {
                    sessionState.postValue(ShoppingSessionState.NoSession)
                }
                is Message.IncomingMessage.TrolleyConnected -> {
                    sessionState.postValue(ShoppingSessionState.Connected)
                }
                is Message.IncomingMessage.UserReady -> {
                    //First moving state

                    postMovingState()
                }
                is Message.IncomingMessage.RouteCalculated -> {
                    route = message.route
                    sh.sendMessage(Message.OutgoingMessage.RouteReceived)
                }
                is Message.IncomingMessage.ReachedPoint -> {
                    consume = reachedPoint(message.id)
                }
                is Message.IncomingMessage.TrolleyAcceptedProduct -> {
                    productAcceptedInternal()
                }
                is Message.IncomingMessage.TrolleyRejectedProduct -> {
                    productRejectedInternal()
                }
            }
            if (!consume) incomingMessages.postValue(message)
        }
    }

    override fun startSession(list: ShoppingList) {
        if (!sh.isConnected) {
            Timber.i("Starting session")
            shoppingList = list
            sessionState.postValue(ShoppingSessionState.Connecting)
            sh.start(RetrofitProvider.root + "/app")
            sh.sendMessage(Message.OutgoingMessage.PlanRoute(list.code))
        }
    }

    override fun endSession() {
        uid = ""
        sh.stop()
    }

    /*
      If we have reached a shelf, consume the message and switch to scanning state
     */
    private fun reachedPoint(id: String): Boolean {
        val pointIndex = route.indexOfFirst { rp -> rp is Route.RoutePoint.Stop && rp.id == id }
        val point = route.getOrNull(pointIndex)
        if (point is Route.RoutePoint.Stop) {
            index = pointIndex
            Timber.i("At stop point")
            sessionState.postValue(ShoppingSessionState.Scanning(point))
            return true
        } else if (point is Route.RoutePoint.Pass || point is Route.RoutePoint.TurnLeft || point is Route.RoutePoint.TurnRight || point is Route.RoutePoint.TurnCenter) {
            // We don't want to change state if a tag scan is triggered while we are scanning
            // TODO: Make sure the Trolley scanning code doesn't post tags twice sequentially
            if (sessionState.value !is ShoppingSessionState.Scanning && sessionState.value !is ShoppingSessionState.Confirming) {
                index = pointIndex
                // We should already be in a NavigatingTo state at this point
                sessionState.postValue(ShoppingSessionState.NavigatingTo(route[index - 1], route[index]))
            }
        } else {
            Timber.i("At other point $point")
        }
        return false
    }

    private fun postMovingState() {
        Timber.i("Moving from ${route[index]} to ${route[index + 1]}")
        sessionState.postValue(ShoppingSessionState.NavigatingTo(route[index], route[index + 1]))
        val point = route.subList(fromIndex = index, toIndex = route.size)
            .firstOrNull { point -> point is Route.RoutePoint.Stop }
        Timber.i("Found point $point")
        if (point is Route.RoutePoint.Stop) {
            val indices = point.productIndices
            remainingRackProducts.clear()
            remainingRackProducts.addAll(shoppingList.products.slice(indices))
            currentRackProducts.postValue(remainingRackProducts)
        }
    }

    /*
     Attempt to match a scanned barcode to a product
     As we are on a local network, making a GET request is currently not an issue
     TODO: Check against cached products for the current shelf
     */
    override suspend fun checkScannedCode(code: String): Product? {
        sh.sendMessage(Message.OutgoingMessage.ProductScanned(code))

        var product = shoppingList.products.firstOrNull { item -> item.product.id == code }?.product
        if (product == null) {
            val fromServer = productLoader.loadProduct(code)
            if (fromServer is Result.Success) {
                product = fromServer.data
            }
        }
        if (product != null) {
            lastScannedProduct.postValue(product)
            sessionState.postValue(ShoppingSessionState.Confirming(product))
        }
        return product
    }

    override fun productAccepted() {
        sh.sendMessage(Message.OutgoingMessage.ProductAccepted(lastScannedProduct.value!!.id))
        productAcceptedInternal()
    }

    /*
     Change session state when product is accepted
     Either by the app or via message from trolley
     */
    private fun productAcceptedInternal() {
        val current = remainingRackProducts.first()
        // Decrement quantity of current product
        remainingRackProducts[0] = current.copy(quantity = current.quantity - 1)
        if (remainingRackProducts.first().quantity == 0) {
            remainingRackProducts.removeAt(0)
        }
        // If there are no products remaining on the rack, navigate to the next point
        if (remainingRackProducts.isEmpty()) {
            currentRackProducts.postValue(null) // Nothing left to observe
            postMovingState()
        } else {
            // Otherwise, we are still in the scanning state
            currentRackProducts.postValue(remainingRackProducts)
            sessionState.postValue(ShoppingSessionState.Scanning(route[index] as Route.RoutePoint.Stop))
        }
    }

    override fun productRejected() {
        sh.sendMessage(Message.OutgoingMessage.ProductRejected(lastScannedProduct.value!!.id))
        productRejectedInternal()
    }

    private fun productRejectedInternal() {
        sessionState.postValue(ShoppingSessionState.Scanning(route[index] as Route.RoutePoint.Stop))
    }

    override fun requestAssistance() {
        sh.sendMessage(Message.OutgoingMessage.RequestHelp)
    }

    // Repeatedly attempt to reconnect to the server
    private fun attemptReconnection() {
        //TODO: How to scope this
        GlobalScope.launch {
            //TODO: Break after some number of reconnection attempts
            while (!sh.isConnected) {
                sh.sendMessage(Message.OutgoingMessage.Reconnect(uid))
                delay(1000)
            }
        }
    }
}