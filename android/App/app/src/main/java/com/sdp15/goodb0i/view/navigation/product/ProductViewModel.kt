package com.sdp15.goodb0i.view.navigation.product

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.view.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.standalone.get

class ProductViewModel : BaseViewModel<Any>() {

    private val sm: ShoppingSession by get<ShoppingSessionManager>()

    val products: MutableLiveData<List<ListItem>> = MutableLiveData()

    override fun bind() {
        sm.state.observeForever(stateObserver)
    }

    fun requestAssistance() = sm.requestAssistance()

    private val stateObserver = Observer<ShoppingSessionState> { state ->
        if (state is ShoppingSessionState.NavigatingTo) {
            transitions.postValue(ProductFragmentDirections.actionItemFragmentToNavigatingToFragment())
        } else if (state is ShoppingSessionState.Scanning) {
            products.postValue(state.toScan)
        } else if (state is ShoppingSessionState.Disconnected) {
            transitions.postValue(ProductFragmentDirections.actionItemFragmentToErrorFragment())
        }
    }

    fun scan() {
        transitions.postValue(ProductFragmentDirections.actionItemFragmentToScannerFragment())
    }

    fun skip() {
        sm.skipProduct()
    }

    fun overrideScan() {
        launch {
            val state = sm.state.value as? ShoppingSessionState.Scanning
            state?.toScan?.first()?.let { product ->
                sm.checkScannedCode(product.product.id)
                sm.productAccepted()
            }

        }

    }
    override fun onCleared() {
        super.onCleared()
        sm.state.removeObserver(stateObserver)
    }
}
