package common.models

import com.google.gson.annotations.SerializedName

/**
 * Product representing
 */
data class ListItem(
    @SerializedName("product") val product: Product,
    @SerializedName("quantity") val quantity: Int)