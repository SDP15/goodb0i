package repository.products

import org.jetbrains.exposed.dao.UUIDTable

object Products : UUIDTable() {
    val name = varchar("name", 255)
    val gtin = varchar("gtin", 13)
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