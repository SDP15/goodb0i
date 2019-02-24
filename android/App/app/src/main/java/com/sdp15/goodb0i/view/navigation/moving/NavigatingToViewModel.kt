package com.sdp15.goodb0i.view.navigation.moving

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.Route
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject
import timber.log.Timber

class NavigatingToViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    val currentProduct: LiveData<ListItem> = sm.currentProduct

    override fun bind() {
        sm.state.observeForever(stateObserver)
    }

    private val stateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            if (state.from is Route.RoutePoint.EntryCollectionPoint) {
                transitions.postValue(NavigatingToFragmentDirections.actionNavigatingToFragmentToItemFragment())
            } else {
                Timber.d("Moving towards RoutePoint ${state.point}")
                //TODO: Update progress display
            }
        } else if (state is ShoppingSessionState.Disconnected) {
            //TODO: Do something about this
        }
        Timber.d("State: $state")
    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(stateObserver)
    }
}