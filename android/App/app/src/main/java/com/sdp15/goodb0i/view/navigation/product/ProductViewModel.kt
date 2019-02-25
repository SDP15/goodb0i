package com.sdp15.goodb0i.view.navigation.product

import androidx.lifecycle.LiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ProductViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    val product: LiveData<ListItem> = sm.currentProduct

    override fun bind() {

    }

    fun scan() {
        transitions.postValue(ProductFragmentDirections.actionItemFragmentToScannerFragment())
    }

    fun repeat() {

    }

    override fun onCleared() {
        super.onCleared()
    }
}
