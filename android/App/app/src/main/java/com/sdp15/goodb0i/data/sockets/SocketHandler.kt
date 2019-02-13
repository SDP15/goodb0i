package com.sdp15.goodb0i.data.sockets

import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class SocketHandler {

    fun start(url: String) {
        val request = Request.Builder().url(url).build()
        val listener = SocketListener()
        val client = OkHttpClient()
        Timber.i("Starting websocket")
        client.newWebSocket(request, listener)
    }

}