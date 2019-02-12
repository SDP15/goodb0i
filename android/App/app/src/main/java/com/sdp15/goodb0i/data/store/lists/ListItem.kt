package com.sdp15.goodb0i.data.store.lists

import com.google.gson.annotations.SerializedName
import com.sdp15.goodb0i.data.store.items.Item

/**
 * Item representing
 */
data class ListItem(
    @SerializedName("product") val item: Item,
    @SerializedName("quantity") val quantity: Int)