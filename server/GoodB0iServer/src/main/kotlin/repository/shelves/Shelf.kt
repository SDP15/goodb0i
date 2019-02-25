package repository.shelves

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import repository.products.Product
import java.util.*

class Shelf(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Shelf>(Shelves)
    var product by Product referencedOn Shelves.product
    var quantity by Shelves.quantity
    var position by Shelves.position
    var rack by Shelves.rack

}