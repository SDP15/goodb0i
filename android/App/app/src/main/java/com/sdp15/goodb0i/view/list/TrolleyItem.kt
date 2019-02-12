package com.sdp15.goodb0i.view.list

import com.sdp15.goodb0i.data.store.items.Item

/**
 * Model for items in the trolley. An added with a quantity
 */
data class TrolleyItem(val item: Item, var count: Int = 0)