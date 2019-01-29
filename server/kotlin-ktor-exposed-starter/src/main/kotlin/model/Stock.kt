package model

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object AllStock : Table() {

    val id = integer("id").primaryKey().autoIncrement()
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


    fun toStock(row: ResultRow) = Stock(
            id = row[AllStock.id],
            name = row[AllStock.name],
            averageSellingUnitWeight = row[AllStock.averageSellingUnitWeight],
            contentsMeasureType = row[AllStock.contentsMeasureType],
            contentsQuantity = row[AllStock.contentsQuantity],
            unitOfSale = row[AllStock.unitOfSale],
            unitQuantity = row[AllStock.unitQuantity],
            department = row[AllStock.department],
            description = row[AllStock.description].split("//"),
            price = row[AllStock.price],
            superDepartment = row[AllStock.superDepartment],
            unitPrice = row[AllStock.unitPrice]

    )


}

data class Stock(
        val id: Int = -1,
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
)

