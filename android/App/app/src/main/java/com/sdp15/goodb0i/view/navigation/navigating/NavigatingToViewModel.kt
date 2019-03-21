package com.sdp15.goodb0i.view.navigation.navigating

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Route
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.get
import timber.log.Timber

class NavigatingToViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSession by get<ShoppingSessionManager>()

    val destination: MutableLiveData<NavigatingToFragment.NavigationDestination> = MutableLiveData()

    override fun bind() {
        sm.state.observeForever(stateObserver)
    }

    fun requestAssistance() = sm.requestAssistance()

    private val stateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            // TODO: Update progress display
            Timber.i("Navigating from ${state.from.index} to ${state.to.index}, at ${state.at.index}")
            if (state.to is Route.RoutePoint.IndexPoint.IdentifiedPoint.End) {
                destination.postValue(
                    NavigatingToFragment.NavigationDestination.EndPoint(
                        distance = state.to.index - state.from.index,
                        progress = state.at.index - state.from.index
                    )
                )
            } else {
                destination.postValue(
                    NavigatingToFragment.NavigationDestination.ShelfRack(
                        distance = state.to.index - state.from.index,
                        progress = state.at.index - state.from.index,
                        toCollect = state.products
                    )
                )
            }
        } else if (state is ShoppingSessionState.Disconnected) {
            transitions.postValue(NavigatingToFragmentDirections.actionNavigatingToFragmentToErrorFragment())
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