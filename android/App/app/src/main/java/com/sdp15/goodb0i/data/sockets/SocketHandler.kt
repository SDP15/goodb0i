package com.sdp15.goodb0i.data.sockets

import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class SocketHandler {

    fun start(url: String, name: String) {
        val request = Request.Builder().url(url).build()
        val listener = SocketListener(name)
        val client = OkHttpClient()
        Timber.i("Starting websocket")
        client.newWebSocket(request, listener)
    }

}