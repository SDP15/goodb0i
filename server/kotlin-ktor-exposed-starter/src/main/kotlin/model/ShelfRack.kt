package model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Racks : IntIdTable() {
    val name = varchar("name", 50)
    val capacity = integer("capacity") // number of shelves
}

class Rack(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Rack>(Racks)

    val name by Racks.name
    val capacity by Racks.capacity

}

object Shelfs : IntIdTable() {
    val productRef = reference("product", Stocks)
    val quantity = (integer("quantity"))
    val position = (integer("position"))
    val rackRef = reference("rack", Racks)


}

class Shelf(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Shelf>(Shelfs)

    val productRef by Shelfs.productRef
    val quantity by Shelfs.quantity
    val position by Shelfs.position
    val rackRef by Shelfs.rackRef
}
