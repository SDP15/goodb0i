package com.sdp15.goodb0i.view.navigation.confirmation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.get

class ItemConfirmationViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSession by get<ShoppingSessionManager>()

    val product = MutableLiveData<Product>()

    override fun bind() {
        sm.state.observeForever(sessionStateObserver)
    }

    private val sessionStateObserver = Observer<ShoppingSessionState> { state ->
        when (state) {
            is ShoppingSessionState.NavigatingTo -> // Scan accepted, there is another products or tills to go to
                transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToNavigatingToFragment())
            is ShoppingSessionState.Scanning -> // Either scan rejected, or item on the same ShelfRack -> ScanningFragment or ItemFragment
                transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToItemFragment())
            is ShoppingSessionState.Confirming -> product.postValue(state.product)
        }
    }

    fun accept() {
        sm.productAccepted()
    }

    fun reject() {
        sm.productRejected()
    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(sessionStateObserver)
    }
}