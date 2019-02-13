package model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Table
import java.util.*


object Lists : UUIDTable() {
    val code = long("code")
    val time = long("time").autoIncrement().nullable()
}

class List(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<List>(Lists)
    var code by Lists.code
    var time by Lists.time
    var products by ListProduct via ListContentsTable
}

/**
 * In order to properly insert products with quantity, ListProducts holds quantities with a reference to a product
 */
object ListProducts : IntIdTable() {
    val product = reference("product_id", Stocks).primaryKey()
    val quantity = integer("quantity")
}

class ListProduct(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ListProduct>(ListProducts)
    var product by Stock referencedOn ListProducts.product
    var quantity by ListProducts.quantity
}

/**
 * Table mapping lists to each of the products in them
 */
object ListContentsTable : Table() {
    val listId = reference("list_id", Lists).primaryKey(0)
    val productId = reference("list_product_id", ListProducts).primaryKey(1)
}



//class ListContent(id: EntityID<Int>) : IntEntity(id) {
//    companion object : IntEntityClass<ListContent>(ListContentsTable)
//    var list by ListContentsTable.listId
//    //var quantity by ListContentsTable.quantity
//    var product by ListContentsTable.productId
//}