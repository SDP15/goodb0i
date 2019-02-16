package repository.lists

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import repository.products.Product

/*
An individual entry in a ShoppingList: A product and a quantity
 */
class ListEntry(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ListEntry>(ListEntries)
    var index by ListEntries.index // The index of the item, to be preserved
    var product by Product referencedOn ListEntries.product
    var quantity by ListEntries.quantity
}