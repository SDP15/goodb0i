package com.sdp15.goodb0i.data.navigation

import androidx.lifecycle.LiveData
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.products.Product

interface ShoppingSessionManager<IN> {

    val incoming: LiveData<IN>

    val currentProduct: LiveData<ListItem>

    val scannedProduct: LiveData<Product>

    val state: LiveData<ShoppingSessionState>

    fun startSession(list: ShoppingList)

    fun endSession()

    suspend fun checkScannedCode(code: String): Product?

    fun productAccepted()

    fun productRejected()

    fun requestAssistance()

}