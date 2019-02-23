package com.sdp15.goodb0i.data.navigation.sockets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.store.RetrofitProvider
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SessionManager(private val sh: SocketHandler<Message.IncomingMessage, Message.OutgoingMessage>) : ShoppingSessionManager<Message.IncomingMessage> {

    private val incomingMessages = MutableLiveData<Message.IncomingMessage>()
    override val incoming: LiveData<Message.IncomingMessage>
        get() = incomingMessages

    private var uid: String = ""

    init {
        //TODO: Handle disconnecting observers
        sh.connectionState.observeForever { state ->
            if (state == SocketHandler.SocketState.ErrorDisconnect) {
                attemptReconnection()
            }
        }
        sh.incomingMessages.observeForever { message ->
            var consume = false
            when (message) {
                is Message.IncomingMessage.Connected -> {
                    uid = message.id
                }
                is Message.IncomingMessage.ReachedPoint -> {

                }
            }
            if (!consume) incomingMessages.postValue(message)
        }
    }

    override fun startSession(list: ShoppingList) {
        if (!sh.isConnected) {
            sh.start(RetrofitProvider.root + "/app")
        }
    }

    override fun endSession() {
        uid = ""
        sh.stop()
    }

    override fun codeScanned(code: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun productAccepted(id: Long) {
        sh.sendMessage(Message.OutgoingMessage.ProductAccepted(id))
    }

    override fun productRejected(id: Long) {
        sh.sendMessage(Message.OutgoingMessage.ProductRejected(id))
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