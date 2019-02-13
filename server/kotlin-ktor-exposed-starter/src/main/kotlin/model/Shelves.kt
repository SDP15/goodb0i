package model

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.dao.UUIDTable

object Shelves : UUIDTable() {
    val product = reference("product_reference", Stocks)
    val quantity = (integer("quantity"))
    val position = (integer("position"))
    val rack = reference("rack", ShelfRacks)
}