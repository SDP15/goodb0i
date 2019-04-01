package com.sdp15.goodb0i.view.navigation.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.price.PriceComputer
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.view.BaseViewModel
import com.sdp15.goodb0i.view.ListDiff
import org.koin.standalone.inject
import timber.log.Timber

class CheckoutViewModel : BaseViewModel<Any>() {
    private val sessionManager: ShoppingSessionManager by inject()
    private val session: ShoppingSession by sessionManager
    private val priceComputer: PriceComputer by inject()

    private val currentProducts = mutableListOf<ListItem>()
    val products =  MutableLiveData<ListDiff<ListItem>>()

    val totalPrice = MutableLiveData<Double>()

    override fun bind() {
        session.state.observeForever(observer)
    }

    private val price: Double
        get() = priceComputer.itemsPrice(currentProducts)

    private val observer = Observer<ShoppingSessionState> { state ->
        Timber.i("Observed state $state")
        if (state is ShoppingSessionState.Checkout) {
            currentProducts.clear()
            currentProducts.addAll(state.products)
            Timber.i("Checkout out with products $products")
            totalPrice.postValue(price)
            products.postValue(ListDiff.All(currentProducts))
        }
    }

    fun disposeSession() {
        session.state.removeObserver(observer)
        session.endSession()
        sessionManager.closeSession()
        transitions.postValue(CheckoutFragmentDirections.actionCompleteFragmentToWelcomeFragment())
    }

    fun incrementItem(product: Product) {
        Timber.i("Incrementing added ${product.name}")
        val i = currentProducts.indexOfFirst { it.product.id == product.id }
        val diff: ListDiff<ListItem>
        if (i == -1) { // Add products to list as it isn't there already
            val ci = ListItem(product, 1)
            currentProducts.add(ci)
            diff = ListDiff.Add(currentProducts, ci)
        } else { // Update count and post to update adapter
            currentProducts[i].quantity++
            diff = ListDiff.Update(currentProducts, currentProducts[i])
        }
        totalPrice.postValue(price)
        products.postValue(diff)
    }

    fun decrementItem(product: Product) {
        Timber.i("Decrementing added ${product.name}")
        val ci = currentProducts.firstOrNull { it.product.id == product.id }
        // If the products doesn't exist in the current list, the user is decrementing an products in search which is at 0
        ci?.apply {
            quantity--
            if (quantity == 0) {
                currentProducts.remove(this)
                products.postValue(ListDiff.Remove(currentProducts, this))
            } else {
                products.postValue(ListDiff.Update(currentProducts, this))
            }
        }
        totalPrice.postValue(price)
    }

    override fun onCleared() {
        super.onCleared()
        session.state.removeObserver(observer)
    }
}