package service

import org.jetbrains.exposed.sql.transactions.transaction
import repository.products.Product
import repository.shelves.Shelf
import repository.shelves.ShelfRack
import repository.shelves.ShelfRacks
import repository.shelves.Shelves

class ShelfService {

    fun getProductsForShelfRack(shelfId: Int): List<Product> = transaction {
        Shelf.find { Shelves.rack eq shelfId }.map { shelf -> shelf.product }
    }

}