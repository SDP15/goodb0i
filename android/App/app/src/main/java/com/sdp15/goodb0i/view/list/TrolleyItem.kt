package com.sdp15.goodb0i.view.list

import com.sdp15.goodb0i.data.store.products.Product

/**
 * Model for items in the trolley. An added with a quantity
 */
data class TrolleyItem(val product: Product, var count: Int = 0)