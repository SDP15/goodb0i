package model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Shelf(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Shelf>(Shelves)
    var product by Shelves.product
    var quantity by Shelves.quantity
    var position by Shelves.position
    var rack by Shelves.rack

}