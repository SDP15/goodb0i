package com.sdp15.goodb0i.data.navigation.sockets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.navigation.Message

class SessionManager(val sh: SocketHandler<Message>) {

    private val incomingMessages = MutableLiveData<Message>()
    val incoming: LiveData<Message>
        get() = incomingMessages

    init {
        sh.connectionState.observeForever {

        }
        sh.incomingMessages.observeForever {

        }
    }

    fun start() {
        if (!sh.isConnected) {
            sh.start("")
        }
    }


}