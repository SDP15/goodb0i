package common.models

import com.google.gson.annotations.SerializedName

data class ShoppingList(
    @SerializedName("code") val code: String,
    @SerializedName("time") val time: Long,
    @SerializedName("products") val products: List<ListItem>)