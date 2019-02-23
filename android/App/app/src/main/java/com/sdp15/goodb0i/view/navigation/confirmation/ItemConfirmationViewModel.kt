package com.sdp15.goodb0i.view.navigation.confirmation

import androidx.lifecycle.LiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ItemConfirmationViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    val scannedProduct: LiveData<Product> = sm.scannedProduct

    override fun bind() {

    }

}