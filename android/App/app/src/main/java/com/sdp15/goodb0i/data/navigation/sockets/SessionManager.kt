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

class SessionManager(
    private val sh: SocketHandler<Message.IncomingMessage, Message.OutgoingMessage>,
    private val productLoader: ProductLoader
) : ShoppingSessionManager<Message.IncomingMessage> {

    private val incomingMessages = MutableLiveData<Message.IncomingMessage>()
    override val incoming: LiveData<Message.IncomingMessage> = incomingMessages

    private var uid: String = ""
    private var route: Route = Route.emptyRoute()
    private var shoppingList: ShoppingList = ShoppingList.emptyList()

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
            var consume = false
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

    private fun reachedPoint(id: String): Boolean {
        val point = route.first { rp -> rp.id == id }
        if (point is Route.RoutePoint.EntryCollectionPoint) {
            sessionState.postValue(ShoppingSessionState.Scanning(point))
            return true
        }
        return false
    }

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


        //TODO: Check against cached products for the current shelf

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