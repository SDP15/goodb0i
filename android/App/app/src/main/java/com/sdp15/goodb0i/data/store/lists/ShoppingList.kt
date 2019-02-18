package com.sdp15.goodb0i.data.store.lists

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShoppingList(
    @SerializedName("code") val code: String,
    @SerializedName("time") val time: Long,
    @SerializedName("products") val products: List<ListItem>) : Parcelable