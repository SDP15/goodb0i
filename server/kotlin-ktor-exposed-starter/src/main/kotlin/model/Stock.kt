package model

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.dao.*
import java.util.*


object Stocks : UUIDTable() {
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

class Stock(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Stock>(Stocks)
    var name by Stocks.name
    var averageSellingUnitWeight by Stocks.averageSellingUnitWeight
    var contentsMeasureType by Stocks.contentsMeasureType
    var contentsQuantity by Stocks.contentsQuantity
    var unitOfSale by Stocks.unitOfSale
    var unitQuantity by Stocks.unitQuantity
    var department by Stocks.department
    var superDepartment by Stocks.superDepartment
    var description by Stocks.description
    var price by Stocks.price
    var unitPrice by Stocks.unitPrice
}

