package model

import org.jetbrains.exposed.sql.Table

object AllStock : Table() {

    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val averageSellingUnitWeight= double("averageSellingUnitWeight")
    val contentsMeasureType= varchar("ContentsMeasureType",20)
    val contentsQuantity= double("contentsQuantity")
    val unitOfSale= integer("UnitOfSale")
    val unitQuantity= varchar("UnitQuantity",20)
    val department= varchar("department",50)
    val description= varchar("description",255)
    val price= double("price")
    val superDepartment= varchar("superDepartment",50)
    val unitprice= double("unitprice")


}

data class Stock(
        val id: Int,
        val name: String,
        val averageSellingUnitWeight: Double,
        val contentsMeasureType: String,
        val contentsQuantity: Double,
        val unitOfSale: Int,
        val unitQuantity: String,
        val department: String,
        val description: List<String>,
        val price: Double,
        val superDepartment: String,
        val unitprice: Double
)


data class NewStock(
        val id: Int?,
        val name: String,
        val averageSellingUnitWeight: Double,
        val contentsMeasureType: String,
        val contentsQuantity: Double,
        val unitOfSale: Int,
        val unitQuantity: String,
        val department: String,
        val description: List<String>,
        val price: Double,
        val superDepartment: String,
        val unitprice: Double
)
