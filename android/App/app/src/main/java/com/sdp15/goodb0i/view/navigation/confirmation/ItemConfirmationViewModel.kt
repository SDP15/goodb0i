package com.sdp15.goodb0i.view.navigation.confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ItemConfirmationViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    val scannedProduct: LiveData<Product> = sm.scannedProduct

    override fun bind() {
        sm.incoming.observeForever(trolleyMessageListener)
    }

    private val trolleyMessageListener = Observer<Message.IncomingMessage> { message ->
        if (message is Message.IncomingMessage.TrolleyAcceptedProduct) {
            accept()
        } else if (message is Message.IncomingMessage.TrolleyRejectedProduct) {
            reject()
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
    }

    fun reject() {

    }

    override fun onCleared() {
        super.onCleared()
        sm.incoming.removeObserver(trolleyMessageListener)
    }
}