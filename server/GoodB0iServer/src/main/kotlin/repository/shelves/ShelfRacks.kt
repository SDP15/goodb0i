package repository.shelves

import org.jetbrains.exposed.dao.IntIdTable

object ShelfRacks : IntIdTable() {
    val capacity = integer("capacity")
    val info = varchar("info", 255)
}