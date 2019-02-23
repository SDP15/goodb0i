package com.sdp15.goodb0i.data.navigation

interface ShoppingSessionManager {

    fun codeScanned(code: String)

    fun productAccepted(id: Long)

    fun productRejected(id: Long)

    fun requestAssistance()

}