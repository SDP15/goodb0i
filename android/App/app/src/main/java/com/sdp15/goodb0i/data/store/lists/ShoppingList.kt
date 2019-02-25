package com.sdp15.goodb0i.data.store.lists

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class ShoppingList(
    @PrimaryKey @SerializedName("code") var code: Long,
    @SerializedName("time") val time: Long,
    @SerializedName("products") val products: List<ListItem>
) : Parcelable {

    companion object {
        fun emptyList() = ShoppingList(0, 0, kotlin.collections.emptyList())
    }

}