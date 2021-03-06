package com.sdp15.goodb0i.data.navigation

import androidx.lifecycle.LiveData
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.products.Product

interface ShoppingSession {

    val state: LiveData<ShoppingSessionState>

    fun startSession(list: ShoppingList)

    fun endSession()

    suspend fun checkScannedCode(code: String): Product?

    fun productAccepted()

    fun productRejected()

    fun skipProduct()

    fun requestAssistance()

}