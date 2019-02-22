package com.sdp15.goodb0i.data.store.lists.cache

import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.coroutines.Deferred

interface ShoppingListStore {

    suspend fun storeList(list: ShoppingList)

    suspend fun loadListsAsync(): List<ShoppingList>

}