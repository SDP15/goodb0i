package model

import org.jetbrains.exposed.sql.Table

object AllStock : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val quantity = integer("quantity")
    val dateUpdated = long("dateUpdated")
}


data class Stock(
        val id: Int,
        val name: String,
        val quantity: Int,
        val dateUpdated: Long
)


data class NewStock(
        val id: Int?,
        val name: String,
        val quantity: Int
)
