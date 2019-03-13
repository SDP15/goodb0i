package com.sdp15.goodb0i.view.navigation.connecting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ShopConnectionViewModel : BaseViewModel<Any>() {

    private val sessionManager: ShoppingSessionManager by inject()
    private lateinit var sm: ShoppingSession
    private lateinit var sl: ShoppingList

    private val builder = StringBuilder()
    val progress = MutableLiveData<Int>()

    override fun bind() {
    }

    fun setShoppingList(list: ShoppingList) {
        sl = list
        sm = sessionManager.startSession()
        sm.startSession(sl)
        sm.state.observeForever(connectionObserver)
    }

    private val connectionObserver = Observer<ShoppingSessionState> { state ->
        when (state) {
            //TODO: Post more information to fragment
            ShoppingSessionState.Connecting -> {
                progress.postValue(0)
            }
            ShoppingSessionState.NegotiatingTrolley -> {
                progress.postValue(2)
            }
            ShoppingSessionState.Connected -> {
                progress.postValue(1)
            }
            ShoppingSessionState.NoSession -> {
                progress.postValue(-1)
            }
            is ShoppingSessionState.NavigatingTo -> {
                progress.postValue(3)
                transitions.postValue(ShopConnectionFragmentDirections.actionShopConnectionFragmentToNavigatingToFragment())
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(connectionObserver)
    }
}