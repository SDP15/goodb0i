package com.sdp15.goodb0i.data.store.price

import com.sdp15.goodb0i.data.store.lists.ListItem

object SimplePriceComputer : PriceComputer {

    override fun itemPrice(item: ListItem): Double = item.product.price * item.quantity

    override fun itemsPrice(items: Collection<ListItem>): Double = items.sumByDouble { it.product.price * it.quantity }
}