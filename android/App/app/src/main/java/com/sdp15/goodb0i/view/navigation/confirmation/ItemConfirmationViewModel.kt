package com.sdp15.goodb0i.view.navigation.confirmation

import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.get

class ItemConfirmationViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSession by get<ShoppingSessionManager>()

    override fun bind() {
        sm.state.observeForever(sessionStateObserver)
    }

    fun requestAssistance() = sm.requestAssistance()

    private val sessionStateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            // Scan accepted, there is another products or tills to go to
            transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToNavigatingToFragment())
        } else if (state is ShoppingSessionState.Scanning) {
            // Either scan rejected, or item on the same ShelfRack -> ScanningFragment or ItemFragment
            //TODO: Check that this pops the ItemConfirmationFragment
            transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToItemFragment())
            //TODO: Add check to go straight back to ScannerFragment ??
            //transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToScannerFragment())
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