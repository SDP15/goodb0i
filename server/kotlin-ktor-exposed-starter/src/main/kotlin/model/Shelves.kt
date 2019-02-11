package model

import org.jetbrains.exposed.dao.IntIdTable

object Shelves : IntIdTable() {
    val product = reference("product_reference", Stocks)
    val quantity = (integer("quantity"))
    val position = (integer("position"))
    val rack = reference("rack", ShelfRacks)
}