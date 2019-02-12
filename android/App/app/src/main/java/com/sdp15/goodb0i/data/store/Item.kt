package com.sdp15.goodb0i.data.store

import com.google.gson.annotations.SerializedName


data class Item(
    @SerializedName("id") val id: String = "",
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


) {
    override fun equals(other: Any?): Boolean {
        return other is Item && other.id == id
    }
}