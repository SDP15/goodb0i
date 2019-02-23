package com.sdp15.goodb0i.data.store.cache

import androidx.lifecycle.LiveData
import com.sdp15.goodb0i.data.store.lists.ShoppingList

interface ShoppingListStore {

    suspend fun storeList(list: ShoppingList)

    fun loadLists(): LiveData<List<ShoppingList>>

}