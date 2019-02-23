package com.sdp15.goodb0i.data.navigation

import androidx.lifecycle.LiveData
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.lists.ShoppingList

interface ShoppingSessionManager<IN> {

    val incoming: LiveData<IN>

    val currentProduct: LiveData<ListItem>

    val state: LiveData<ShoppingSessionState>

    fun startSession(list: ShoppingList)

    fun endSession()



    fun codeScanned(code: String)

    fun productAccepted(id: Long)

    fun productRejected(id: Long)

    fun requestAssistance()

}