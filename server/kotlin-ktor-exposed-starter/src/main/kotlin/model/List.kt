package model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Table

object List : IntIdTable() {
    val code = long("code")
    val time = integer("time")
}

class ListCode(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ListCode>(List)
    val code by List.code
    val time by List.time
}


object ListContentsTable : Table() {
    val listId = reference("list_id", List.id).primaryKey(0)
    val quantity = integer("quantity")
    val productId = reference("product_id", Stocks.id).primaryKey(1)
}

