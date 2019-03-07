package repository.lists

import org.jetbrains.exposed.dao.*


object ShoppingLists : UUIDTable() {
    val code = long("code")
    val time = long("time")
}

