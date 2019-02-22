package com.sdp15.goodb0i.view.list.confirmation

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.store.lists.ShoppingList

class ListConfirmationViewModel : BaseViewModel<Any>() {

    override fun bind() {

    }

    val price = MutableLiveData<Double>()
    val code = MutableLiveData<Long>()
    val time = MutableLiveData<Long>()

    fun setShoppingList(list: ShoppingList) {
        //TODO: Abstract into price computer
        price.postValue(list.products.sumByDouble { it.quantity * it.product.price })
        code.postValue(list.code)
        time.postValue(list.time)

    }


}