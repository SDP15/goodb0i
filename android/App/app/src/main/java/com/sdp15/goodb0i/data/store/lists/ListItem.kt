package com.sdp15.goodb0i.data.store.lists

import com.google.gson.annotations.SerializedName
import com.sdp15.goodb0i.data.store.products.Product

/**
 * Product representing
 */
data class ListItem(
    @SerializedName("product") val product: Product,
    @SerializedName("quantity") val quantity: Int)