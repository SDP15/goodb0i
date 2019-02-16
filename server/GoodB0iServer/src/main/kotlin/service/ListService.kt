package service

import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import repository.lists.ListEntry
import repository.lists.ShoppingList
import repository.lists.ShoppingLists
import repository.products.Product
import repository.products.Products
import java.util.*
import kotlin.random.Random

class ListService {


    fun createList(ids: List<String>, quantities: List<Int>): ShoppingList? =
            transaction {
                println("Beginning list creation transaction")
                // Find all Product matching UUID strings
                val matchingProducts = Product.find { Products.id inList ids.map(UUID::fromString) }
                //TODO: Some sort of error if an item does not exist
                assert(matchingProducts.count() == quantities.size)
                // Insert ListEntry rows with the quantities
                val listProducts =
                        matchingProducts.zip(quantities).map { (p, q) ->
                            ListEntry.new {
                                product = p
                                quantity = q
                            }
                        }
                // Create the list with the ListEntries we just created
                val list = ShoppingList.new {
                    code = Random.nextLong(0, 1000000)
                    time = System.currentTimeMillis()
                    products = SizedCollection(listProducts)
                }

                println("ShoppingList created ${list.id.value}. Code ${list.code}")
                return@transaction list
            }

    fun loadList(code: Long): ShoppingList? = transaction {
        println("Loading list for code $code")
        return@transaction ShoppingList.find { ShoppingLists.code eq code }.limit(1).firstOrNull()
    }

}