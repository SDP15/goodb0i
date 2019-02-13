package com.sdp15.goodb0i.data.sockets

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber

class SocketListener : WebSocketListener() {

    private var count = 0

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Timber.i("Socket opened $response")
        webSocket.send("Some text from the phone")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Timber.e(t, "Socket failure")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Timber.i("Socket closing $reason")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        count++
        Timber.i("Received message $text")
        webSocket.send("Some text from phone $count")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        Timber.i("Received bytestring $bytes")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Timber.i("Socket closed for reason $reason")
    }
}