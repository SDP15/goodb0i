package com.sdp15.goodb0i.data.store.price

import com.sdp15.goodb0i.data.store.lists.ListItem

interface PriceComputer {

    fun itemPrice(item: ListItem): Double

    fun itemsPrice(items: Collection<ListItem>): Double

}