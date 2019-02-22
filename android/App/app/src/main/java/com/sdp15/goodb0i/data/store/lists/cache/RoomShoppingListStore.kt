package com.sdp15.goodb0i.data.store.lists.cache

import com.sdp15.goodb0i.data.store.lists.ShoppingList
import timber.log.Timber

class RoomShoppingListStore(val dao: ListDAO) : ShoppingListStore {

    override suspend fun storeList(list: ShoppingList) {
        Timber.i("Storing list $list")
        dao.insert(list)
    }

    override suspend fun loadListsAsync(): List<ShoppingList> = dao.loadAll()
}