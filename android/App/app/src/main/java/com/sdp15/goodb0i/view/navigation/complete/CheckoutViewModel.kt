package com.sdp15.goodb0i.view.navigation.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class CheckoutViewModel : BaseViewModel<Any>() {

    private val session: ShoppingSession by inject()

    val products =  MutableLiveData<List<ListItem>>()

    override fun bind() {
        session.state.observeForever(observer)
    }

    private val observer = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.Checkout) {

        }
    }

    override fun onCleared() {
        super.onCleared()
        session.state.removeObserver(observer)
    }
}