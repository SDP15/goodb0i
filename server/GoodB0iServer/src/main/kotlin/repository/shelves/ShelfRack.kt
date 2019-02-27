package repository.shelves

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class ShelfRack(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ShelfRack>(ShelfRacks)

    var capacity by ShelfRacks.capacity
    var info by ShelfRacks.info
    val shelves by Shelf referrersOn Shelves.rack
}
