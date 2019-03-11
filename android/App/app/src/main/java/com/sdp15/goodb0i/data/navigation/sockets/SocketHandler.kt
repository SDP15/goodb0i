package com.sdp15.goodb0i.data.navigation.sockets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.*
import okio.ByteString
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/*
 Handles messages in and out of a WebSocket
 */
class SocketHandler<IN, OUT>(private val transform: SocketMessageTransformer<IN, OUT>) {

    // For direct thread-safe access to the current socket state
    private val connected = AtomicBoolean(false)
    val isConnected: Boolean
        get() = connected.get()
    private var socket: WebSocket? = null

    // Open the socket
    fun start(url: String) {
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        Timber.i("Starting websocket for $url")
        socket = client.newWebSocket(request, SocketListener())
    }

    fun stop() {
        //https://github.com/Luka967/websocket-close-codes
        socket?.close(1000, null)
    }

    private fun onMessageReceived(message: String) {
        Timber.i("Message received $message")
        messages.postValue(transform.transformIncoming(message))
    }

    /*
    Privately implements OKHTTP3 WebSocketListener
     */
    private inner class SocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            connected.set(true)
            Timber.i("Socket opened")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Timber.e(t, "Socket failure")
            connected.set(false)
            state.postValue(SocketState.ErrorDisconnect)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Timber.i("Socket closing $reason")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            onMessageReceived(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            onMessageReceived(bytes.utf8())
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            connected.set(false)
            // See here https://github.com/Luka967/websocket-close-codes
            if (code in 1000..1001) {
                state.postValue(SocketState.Disconnected)
            } else {
                state.postValue(SocketState.ErrorDisconnect)
            }
            Timber.i("Socket closed")
        }
    }

    private val messages = MutableLiveData<IN>()

    val incomingMessages: LiveData<IN>
        get() = messages

    private val state = MutableLiveData<SocketState>()

    val connectionState: LiveData<SocketState>
        get() = state

    fun sendMessage(message: OUT) {
        socket?.send(transform.transformOutgoing(message))
    }

    /**
     * Responsible for transforming WebSocket text frames to and from more usable types
     */
    interface SocketMessageTransformer<IN, OUT> {

        fun transformIncoming(message: String): IN

        fun transformOutgoing(message: OUT): String

    }

    /*
    TODO: This could do with some more included information to allow for better error handling
     */
    enum class SocketState {
        Connected,  // Connection open
        Disconnected, // Connection not open
        ErrorDisconnect // Connection not open due to an error
    }

}