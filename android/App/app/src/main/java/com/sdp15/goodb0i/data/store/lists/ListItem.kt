package com.sdp15.goodb0i.data.store.lists

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.sdp15.goodb0i.data.store.products.Product
import kotlinx.android.parcel.Parcelize

/**
 * Item in shopping list
 */
@Parcelize
@Entity
data class ListItem(
    @Embedded @SerializedName("product") var product: Product,
    @SerializedName("quantity") var quantity: Int = 0
) : Parcelable {

    operator fun plus(quantityIncrease: Int): ListItem = ListItem(product, quantity + quantityIncrease)

    operator fun minus(quantityDecrease: Int): ListItem = ListItem(product, quantity - quantityDecrease)
}