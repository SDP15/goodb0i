package repository.lists

import org.jetbrains.exposed.sql.Table

/**
 * Table mapping lists to the ListEntries in them
 */
object ListContentsTable : Table() {
    val list = reference("list_", ShoppingLists).primaryKey(0)
    val entry = reference("list_entry", ListEntries).primaryKey(1)
}