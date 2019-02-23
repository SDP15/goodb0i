package com.sdp15.goodb0i.view.navigation.moving

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class NavigatingToViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    val currentProduct: LiveData<ListItem> = sm.currentProduct

    override fun bind() {
        sm.incoming.observeForever(messageObserver)
    }

    private val messageObserver = Observer<Message.IncomingMessage> { message ->
        if (message is  Message.IncomingMessage.ReachedPoint) {

        }
    }

    override fun onCleared() {
        super.onCleared()
        sm.incoming.removeObserver(messageObserver)
    }
}