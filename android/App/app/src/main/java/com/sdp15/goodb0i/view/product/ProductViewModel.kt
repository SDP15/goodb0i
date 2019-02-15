package com.sdp15.goodb0i.view.product

import com.sdp15.goodb0i.BaseViewModel

class ProductViewModel : BaseViewModel<ProductViewModel.ItemAction>() {

    override fun bind() {

    }

    sealed class ItemAction {}
}
