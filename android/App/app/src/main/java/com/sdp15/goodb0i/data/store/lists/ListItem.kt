package com.sdp15.goodb0i.data.store.lists

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.sdp15.goodb0i.data.store.products.Product
import kotlinx.android.parcel.Parcelize

/**
 * Product representing
 */
@Parcelize
data class ListItem(
    @SerializedName("product") val product: Product,
    @SerializedName("quantity") var quantity: Int) : Parcelable