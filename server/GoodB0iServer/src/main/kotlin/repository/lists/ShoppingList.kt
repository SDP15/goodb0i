package repository.lists

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*


class ShoppingList(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShoppingList>(ShoppingLists)

    var code by ShoppingLists.code
    var time by ShoppingLists.time
    var products by ListEntry via ListContentsTable

}