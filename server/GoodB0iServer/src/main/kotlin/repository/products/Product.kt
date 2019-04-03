package repository.products

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

/**
 * A product that the store *may* have
 * "Stock" was a bad name, as that implies what the store has *in* products, whereas a shopping list
 * should be able to contain any product that a store stocks
 */
class Product(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Product>(Products)
    var gtin by Products.gtin
    var name by Products.name
    var averageSellingUnitWeight by Products.averageSellingUnitWeight
    var contentsMeasureType by Products.contentsMeasureType
    var contentsQuantity by Products.contentsQuantity
    var unitOfSale by Products.unitOfSale
    var unitQuantity by Products.unitQuantity
    var department by Products.department
    var superDepartment by Products.superDepartment
    var description by Products.description
    var price by Products.price
    var unitPrice by Products.unitPrice
}

