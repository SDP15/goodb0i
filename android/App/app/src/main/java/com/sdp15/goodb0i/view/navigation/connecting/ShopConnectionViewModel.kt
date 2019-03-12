package com.sdp15.goodb0i.view.navigation.connecting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject
import timber.log.Timber

class ShopConnectionViewModel : BaseViewModel<Any>() {

    private val sessionManager: ShoppingSessionManager by inject()
    private lateinit var sm: ShoppingSession
    private lateinit var sl: ShoppingList

    private val builder = StringBuilder()
    val log = MutableLiveData<String>()

    override fun bind() {
    }

    fun setShoppingList(list: ShoppingList) {
        sl = list
        sm = sessionManager.startSession()
        sm.startSession(sl)
        sm.state.observeForever(connectionObserver)
    }

    private fun log(message: String) {
        Timber.i(message)
        builder.append(message)
        builder.append('\n')
        log.postValue(builder.toString())
    }

    private val connectionObserver = Observer<ShoppingSessionState> { state ->
        when (state) {
            //TODO: Post more information to fragment
            ShoppingSessionState.Connecting -> {
                log("Connecting")
            }
            ShoppingSessionState.NegotiatingTrolley -> {
                log("Negotiating trolley")
            }
            ShoppingSessionState.Connected -> {
                log("Connected")
            }
            ShoppingSessionState.NoSession -> {
                log("No session")
            }
            is ShoppingSessionState.NavigatingTo -> {
                transitions.postValue(ShopConnectionFragmentDirections.actionShopConnectionFragmentToNavigatingToFragment())
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(connectionObserver)
    }
}