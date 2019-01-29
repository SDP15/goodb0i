package model

import org.jetbrains.exposed.sql.Table


object ShelfRacks : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val capacity = integer("capacity")
    val info = varchar("info", 255)

}


data class ShelfRack(val id: Int,val capacity: Int, val info: String)