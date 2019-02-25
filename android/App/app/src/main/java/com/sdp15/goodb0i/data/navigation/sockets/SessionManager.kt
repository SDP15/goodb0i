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

    private val currentListProduct = MutableLiveData<ListItem>()
    override val currentProduct: LiveData<ListItem> = currentListProduct

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
                is Message.IncomingMessage.TrolleyConnected -> {
                    sessionState.postValue(ShoppingSessionState.Connected)
                }
                is Message.IncomingMessage.RouteCalculated -> {
                    route = message.route
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
            shoppingList = list
            sessionState.postValue(ShoppingSessionState.Connecting)
            sh.start(RetrofitProvider.root + "/app")
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
        val pointIndex = route.indexOfFirst { rp -> rp.id == id }
        val point = route.getOrNull(pointIndex)
        if (point is Route.RoutePoint.EntryCollectionPoint) {
            index = pointIndex
            sessionState.postValue(ShoppingSessionState.Scanning(point))
            return true
        } else if (point is Route.RoutePoint.Pass || point is Route.RoutePoint.TurnLeft || point is Route.RoutePoint.TurnRight) {
            // We don't want to change state if a tag scan is triggered while we are scanning
            // TODO: Make sure the Trolley scanning code doesn't post tags twice sequentially
            if (sessionState.value !is ShoppingSessionState.Scanning && sessionState.value !is ShoppingSessionState.Confirming) {
                index = pointIndex
                // We should already be in a NavigatingTo state at this point
                sessionState.postValue(ShoppingSessionState.NavigatingTo(route[index - 1], route[index]))
            }
        }
        return false
    }

    private fun postMovingState() {
        sessionState.postValue(ShoppingSessionState.NavigatingTo(route[index], route[index + 1]))
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
        //TODO: Move to next Either Scanning or NavigatingTo state
        val next = route[index + 1]
        if (next is Route.RoutePoint.EntryCollectionPoint) {
            sessionState.postValue(ShoppingSessionState.Scanning(next))
        } else {
            //TODO: End state
            postMovingState()
        }
    }

    override fun productRejected() {
        sh.sendMessage(Message.OutgoingMessage.ProductRejected(lastScannedProduct.value!!.id))
        productRejectedInternal()
    }

    private fun productRejectedInternal() {
        sessionState.postValue(ShoppingSessionState.Scanning(route[index] as Route.RoutePoint.EntryCollectionPoint))
    }

    override fun requestAssistance() {
        sh.sendMessage(Message.OutgoingMessage.RequestHelp)
    }

    // Repeatedly attempt to reconnect to the server
    private fun attemptReconnection() {
        GlobalScope.launch {
            //TODO: Break after some number of reconnection attempts
            while (!sh.isConnected) {
                sh.sendMessage(Message.OutgoingMessage.Reconnect(uid))
                delay(1000)
            }
        }
    }
}