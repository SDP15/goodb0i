package repository.shelves

import org.jetbrains.exposed.dao.UUIDTable
import repository.products.Products

object Shelves : UUIDTable() {
    val product = reference("product_reference", Products)
    val quantity = (integer("quantity"))
    val position = (integer("position"))
    val rack = reference("rack", ShelfRacks)
}