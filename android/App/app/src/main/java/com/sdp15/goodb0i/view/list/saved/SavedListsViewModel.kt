package com.sdp15.goodb0i.view.list.saved

import com.sdp15.goodb0i.data.store.cache.ShoppingListStore
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class SavedListsViewModel : BaseViewModel<Any>() {

    private val listStore: ShoppingListStore by inject()

    val lists = listStore.loadLists()

    override fun bind() {

    }

    fun open(list: ShoppingList) {
        transitions.postValue(SavedListsFragmentDirections.actionViewShoppingListToListConfirmationFragment(list))
    }

}