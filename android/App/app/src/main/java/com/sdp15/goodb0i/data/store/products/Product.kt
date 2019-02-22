package com.sdp15.goodb0i.data.store.products

import android.os.Parcelable
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("averageSellingUnitWeight") val averageSellingUnitWeight: Double,
    @SerializedName("ContentsMeasureType") val contentsMeasureType: String,
    @SerializedName("contentsQuantity") val contentsQuantity: Double,
    @SerializedName("UnitOfSale") val unitOfSale: Int,
    @SerializedName("UnitQuantity") val unitQuantity: String,
    @SerializedName("department") val department: String,
    @SerializedName("description") val description: List<String>,
    @SerializedName("price") val price: Double,
    @SerializedName("superDepartment") val superDepartment: String,
    @SerializedName("unitPrice") val unitPrice: Double


) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is Product && other.id == id
    }
}