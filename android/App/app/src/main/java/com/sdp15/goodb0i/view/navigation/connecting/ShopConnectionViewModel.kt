package com.sdp15.goodb0i.view.navigation.connecting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.view.BaseViewModel
import com.sdp15.goodb0i.view.list.confirmation.ListConfirmationFragmentDirections
import org.koin.standalone.inject
import java.lang.StringBuilder

class ShopConnectionViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()
    private lateinit var sl: ShoppingList

    private val builder = StringBuilder()
    val log = MutableLiveData<String>()

    override fun bind() {
        sm.state.observeForever(connectionObserver)
    }

    fun setShoppingList(list: ShoppingList) {
        sl = list
        sm.startSession(sl)
    }

    private fun log(message: String) {
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
                transitions.postValue(ShopConnectionFragmentDirections.actionShopConnectionFragmentToNavigatingToFragment())
                //TODO: Should we remove the observer here?
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(connectionObserver)
    }
}