package repository.lists

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import repository.products.Product
import java.util.*


class ShoppingList(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShoppingList>(ShoppingLists)

    var code by ShoppingLists.code
    var time by ShoppingLists.time
    var products by ListEntry via ListContentsTable

    val orderedProducts: SizedIterable<ListEntry>
            get() = products.orderBy(ListEntries.index to SortOrder.ASC)

}