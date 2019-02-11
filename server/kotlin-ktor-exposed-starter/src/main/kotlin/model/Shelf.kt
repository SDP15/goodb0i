package model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Shelf(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Shelf>(Shelves)
    val product by Shelves.product
    val quantity by Shelves.quantity
    val position by Shelves.position
    val rack by Shelves.rack

}