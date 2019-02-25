package repository.lists

import org.jetbrains.exposed.dao.IntIdTable
import repository.products.Products

/**
 * In order to properly insert products with quantity, ListEntries holds quantities with a reference to a product
 */
object ListEntries : IntIdTable() {
    val product = reference("product_id", Products).primaryKey()
    val quantity = integer("quantity")
    val index = integer("index")
}