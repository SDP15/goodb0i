package com.sdp15.goodb0i.view.navigation.navigating

import androidx.lifecycle.MutableLiveData
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

    val destination: MutableLiveData<NavigatingToFragment.NavigationDestination> = MutableLiveData()

    override fun bind() {
        sm.state.observeForever(stateObserver)
        sm.currentProducts.observeForever(productsObserver)
    }

    private val productsObserver = Observer<List<ListItem>> { list ->
        destination.postValue(NavigatingToFragment.NavigationDestination.ShelfRack(list))
    }

    private val stateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            // TODO: Update progress display
            if (state.point is Route.RoutePoint.End) {
                destination.postValue(NavigatingToFragment.NavigationDestination.EndPoint)
            }
        } else if (state is ShoppingSessionState.Disconnected) {
            //TODO: Do something about this
        } else if (state is ShoppingSessionState.Scanning) {
            transitions.postValue(NavigatingToFragmentDirections.actionNavigatingToFragmentToItemFragment())
        }
        Timber.d("State: $state")
    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(stateObserver)
        sm.currentProducts.removeObserver(productsObserver)
    }
}