package com.sdp15.goodb0i.view.list.confirmation

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.price.PriceComputer
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ListConfirmationViewModel : BaseViewModel<Any>() {

    private val priceComputer: PriceComputer by inject()
    private lateinit var sl: ShoppingList

    override fun bind() {

    }


    //TODO: Replace with LiveData<ShoppingList> and abstract some formatter
    val price = MutableLiveData<Double>()
    val code = MutableLiveData<Long>()
    val time = MutableLiveData<Long>()

    fun setShoppingList(list: ShoppingList) {
        sl = list
        price.postValue(priceComputer.itemsPrice(sl.products))
        code.postValue(list.code)
        time.postValue(list.time)
    }


}