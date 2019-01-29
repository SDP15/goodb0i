package com.sdp15.goodb0i.data.store

import com.google.gson.annotations.SerializedName


data class Item(
    @SerializedName("AverageSellingUnitWeight")
    val averageSellingUnitWeight: Double,
    @SerializedName("ContentsMeasureType")
    val contentsMeasureType: String,
    @SerializedName("ContentsQuantity")
    val contentsQuantity: Int,
    @SerializedName("UnitOfSale")
    val unitOfSale: Int,
    @SerializedName("UnitQuantity")
    val unitQuantity: String,
    @SerializedName("department")
    val department: String,
    @SerializedName("description")
    val description: List<String>,
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("superDepartment")
    val superDepartment: String,
    @SerializedName("unitprice")
    val unitprice: Double
)