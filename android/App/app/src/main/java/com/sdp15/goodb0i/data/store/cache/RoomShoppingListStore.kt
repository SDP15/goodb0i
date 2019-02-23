package com.sdp15.goodb0i.data.store.cache

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class RoomShoppingListStore(private val dao: ListDAO) : ShoppingListStore {

    override suspend fun storeList(list: ShoppingList) {
        Timber.i("Storing list $list")
        dao.insert(list)
    }

    private val list = MutableLiveData<List<ShoppingList>>()

    override fun loadLists(): LiveData<List<ShoppingList>> {
        GlobalScope.launch(Dispatchers.IO) {
            list.postValue(dao.loadAll())
        }
        return list
    }
}