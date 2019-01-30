package model

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Stocks : IntIdTable() {

    val name = varchar("name", 255)
    val averageSellingUnitWeight = double("averageSellingUnitWeight")
    val contentsMeasureType = varchar("ContentsMeasureType", 20)
    val contentsQuantity = double("contentsQuantity")
    val unitOfSale = integer("UnitOfSale")
    val unitQuantity = varchar("UnitQuantity", 20)
    val department = varchar("department", 50)
    val description = text("description")
    val price = double("price")
    val superDepartment = varchar("superDepartment", 50)
    val unitPrice = double("unitPrice")


}

class Stock(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Stock>(Stocks)

    var name by Stocks.name
    var averageSellingUnitWeight by Stocks.averageSellingUnitWeight
    var contentsMeasureType by Stocks.contentsMeasureType
    var contentsQuantity by Stocks.contentsQuantity
    var unitOfSale by Stocks.unitOfSale
    var unitQuantity by Stocks.unitQuantity
    var department by Stocks.department
    var description by Stocks.description
    var price by Stocks.price
    var superDepartment by Stocks.superDepartment
    var unitPrice by Stocks.unitPrice
}

//data class Stock(
//        val id: Int = -1, // Default id. Not ideal, but nicer than nullable
//        @SerializedName("name") val name: String,
//        @SerializedName("averageSellingUnitWeight") val averageSellingUnitWeight: Double,
//        @SerializedName("ContentsMeasureType") val contentsMeasureType: String,
//        @SerializedName("contentsQuantity") val contentsQuantity: Double,
//        @SerializedName("UnitOfSale") val unitOfSale: Int,
//        @SerializedName("UnitQuantity") val unitQuantity: String,
//        @SerializedName("department") val department: String,
//        @SerializedName("description") val description: List<String>,
//        @SerializedName("price") val price: Double,
//        @SerializedName("superDepartment") val superDepartment: String,
//        @SerializedName("unitPrice") val unitPrice: Double
//)

