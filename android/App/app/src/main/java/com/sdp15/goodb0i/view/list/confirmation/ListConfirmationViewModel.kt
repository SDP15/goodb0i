package com.sdp15.goodb0i.view.list.confirmation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.view.BaseViewModel
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import org.koin.standalone.inject

class ListConfirmationViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()
    private lateinit var sl: ShoppingList

    override fun bind() {

    }



    //TODO: Replace with LiveData<ShoppingList> and abstract some formatter
    val price = MutableLiveData<Double>()
    val code = MutableLiveData<Long>()
    val time = MutableLiveData<Long>()

    fun setShoppingList(list: ShoppingList) {
        sl = list
        //TODO: Abstract into price computer
        price.postValue(list.products.sumByDouble { it.quantity * it.product.price })
        code.postValue(list.code)
        time.postValue(list.time)

    }

    private val connectionObserver = Observer<ShoppingSessionState> { state ->
        when (state) {
            //TODO: Post more information to fragment
            ShoppingSessionState.Connecting -> {

            }
            ShoppingSessionState.NegotiatingTrolley -> {

            }
            ShoppingSessionState.Connected -> {
                transitions.postValue(ListConfirmationFragmentDirections.actionListConfirmationFragmentToNavigatingToFragment())
                //TODO: Should we remove the observer here?
            }
        }

    }

    fun startNavigation() {
        sm.state.observeForever(connectionObserver)
        sm.startSession(sl)
    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(connectionObserver)
    }
}