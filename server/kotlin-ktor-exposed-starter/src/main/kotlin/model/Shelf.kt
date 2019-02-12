package model

import org.jetbrains.exposed.dao.*
import java.util.*

class Shelf(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Shelf>(Shelves)
    var product by Shelves.product
    var quantity by Shelves.quantity
    var position by Shelves.position
    var rack by Shelves.rack

}