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
        val point = route.first { rp -> rp.id == id }
        if (point is Route.RoutePoint.EntryCollectionPoint) {
            sessionState.postValue(ShoppingSessionState.Scanning(point))
            return true
        }
        return false
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
        if (product != null) lastScannedProduct.postValue(product)

        return product
    }

    override fun productAccepted() {
        sh.sendMessage(Message.OutgoingMessage.ProductAccepted(lastScannedProduct.value!!.id))
    }

    override fun productRejected() {
        sh.sendMessage(Message.OutgoingMessage.ProductRejected(lastScannedProduct.value!!.id))
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