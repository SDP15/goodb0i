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

                println("Matching product ids ${matchingProducts.map { it.id.value.toString() }}")
                //TODO: Some sort of error if an item does not exist
                assert(matchingProducts.count() == quantities.size)

                // Re-order the received products to match the ordered list sent to us
                val orderedProducts = ids.map { id -> matchingProducts.find { id == it.id.value.toString() }!! }
                // Insert ListEntry rows with the quantities
                val listProducts =
                        orderedProducts.zip(quantities).mapIndexed { i, (p, q) ->
                            ListEntry.new {
                                index = i
                                product = p
                                quantity = q
                            }
                        }
                // Create the list with the ListEntries we just created
                val generatedCode = Random.nextLong(1000000, 9999999)
                val list = ShoppingList.new {
                    code = generatedCode
                    time = System.currentTimeMillis()
                    products = SizedCollection(listProducts)
                }

                println("ShoppingList created ${list.id.value}. Code ${list.code}")
                return@transaction list
            }

    fun loadList(code: Long): ShoppingList? = transaction {
        println("Loading list for code $code") //
        return@transaction ShoppingList.find { ShoppingLists.code eq code }.limit(1).firstOrNull()
    }

}