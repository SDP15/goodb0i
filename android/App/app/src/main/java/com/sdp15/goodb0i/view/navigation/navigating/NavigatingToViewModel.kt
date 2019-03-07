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
    }

    private val stateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            // TODO: Update progress display
            Timber.i("Navigating from ${state.from.index} to ${state.to.index}, at ${state.at.index}")
            if (state.to is Route.RoutePoint.IndexPoint.End) {
                destination.postValue(
                    NavigatingToFragment.NavigationDestination.EndPoint(
                        distance = state.to.index - state.from.index,
                        progress = state.at.index - state.from.index
                    )
                )
            } else {
                //TODO: Cleaner way to do this mess, we really just want to get the
                // products once, as they won't change while we are navigating
                sm.currentProducts.observeForever(object : Observer<List<ListItem>> {
                    override fun onChanged(list: List<ListItem>) {
                        destination.postValue(
                            NavigatingToFragment.NavigationDestination.ShelfRack(
                                distance = state.to.index - state.from.index,
                                progress = state.at.index - state.from.index,
                                toCollect = list
                            )
                        )
                        sm.currentProducts.removeObserver(this)
                    }
                })
            }
        } else if (state is ShoppingSessionState.Disconnected) {
            //TODO: Do something about this
        } else if (state is ShoppingSessionState.Scanning) {
            transitions.postValue(NavigatingToFragmentDirections.actionNavigatingToFragmentToItemFragment())
        } else if (state is ShoppingSessionState.Checkout) {
            transitions.postValue(NavigatingToFragmentDirections.actionNavigatingToFragmentToCompleteFragment())
        }
        Timber.d("State: $state")
    }

    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(stateObserver)
    }
}