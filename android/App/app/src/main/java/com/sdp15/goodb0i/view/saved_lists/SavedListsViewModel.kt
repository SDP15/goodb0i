package com.sdp15.goodb0i.view.saved_lists

import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.store.lists.cache.ShoppingListStore
import org.koin.standalone.inject

class SavedListsViewModel : BaseViewModel<Any>() {

    private val listStore: ShoppingListStore by inject()

    val lists = listStore.loadLists()

    override fun bind() {
    }


}