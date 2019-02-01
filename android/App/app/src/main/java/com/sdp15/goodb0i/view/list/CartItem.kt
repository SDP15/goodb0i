package com.sdp15.goodb0i.view.list

import com.sdp15.goodb0i.data.store.Item

data class CartItem(val item: Item, var count: Int = 0)