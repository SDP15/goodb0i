package com.sdp15.goodb0i.view.navigation.confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ItemConfirmationViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()
    val scannedProduct: LiveData<Product> = sm.scannedProduct

    override fun bind() {
        sm.state.observeForever(sessionStateObserver)
    }

    private val sessionStateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            // Scan accepted, there is another product or tills to go to
            transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToNavigatingToFragment())
        } else if (state is ShoppingSessionState.Scanning) {
            // Either scan rejected, or item on the same ShelfRack -> ScanningFragment or ItemFragment
            //TODO: Check that this pops the ItemConfirmationFragment
            transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToItemFragment())
            //TODO: Add check to go straight bak to ScannerFragment ??
            //transitions.postValue(ItemConfirmationFragmentDirections.actionConfirmationFragmentToScannerFragment())
        }
    }

    fun accept() {
        /*
        TODO: We need to decide how we decide which screen to move to
        If we have accepted, we should destroy the previous fragments and move
        - If there is a new item, we check if it is on the same ShelfRack and move to either ProductFragment or
          NavigatingToFragment
        - If there is no new item, we move to a NavigatingToFragment for the tills
        If we have rejected, we should navigate back to the scanner fragment which should start scanning again
        */
        sm.productAccepted()
    }

    fun reject() {
        sm.productRejected()
    }

    override fun onCleared() {
        super.onCleared()
        sm.state.observeForever(sessionStateObserver)
    }
}