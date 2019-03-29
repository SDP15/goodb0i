package service

import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import repository.lists.*
import repository.products.Product
import repository.products.Products
import java.util.*
import kotlin.random.Random

class ListService {

    sealed class ListServiceResponse {

        data class ListResponse(val list: ShoppingList) : ListServiceResponse()

        sealed class ListServiceError : ListServiceResponse() {

            object ListNotFound : ListServiceError()

            data class ProductsNotFound(val products: List<UUID>) : ListServiceError()

        }

    }


    fun editList(existingCode: Long, entries: List<Pair<UUID, Int>>): ListServiceResponse =
            transaction {
                // Check for existing list and remove all of its entries
                val existing = ShoppingList.find { ShoppingLists.code eq existingCode }.limit(1)
                if (existing.count() == 0) {
                    println("Didn't find existing list with existingCode $existingCode")
                    return@transaction ListServiceResponse.ListServiceError.ListNotFound
                }
                val list = existing.first()
                // Remove existing ListEntries
                ListContentsTable.deleteWhere { ListContentsTable.entry inList list.products.map { it.id } }
                list.products.forEach { it.delete() }
                ListEntries.deleteWhere { ListEntries.id inList list.products.map { it.id } }

                // TODO: This is a duplicate of the createList code.
                // See if there is a nice way to extract the creation along with the error
                // Perhaps return ListServiceError?
                val ids = entries.map { it.first }
                val matchingProducts = Product.find { Products.id inList ids }

                if (matchingProducts.count() != ids.size) {
                    val notFound = ids.filterNot { id -> matchingProducts.any { product -> product.id.value == id } }
                    return@transaction ListServiceResponse.ListServiceError.ProductsNotFound(notFound)
                }
                // Re-order the received products to match the ordered list sent to us
                val orderedProducts = entries.map { entry ->
                    Pair(matchingProducts.find { product -> entry.first == product.id.value }!!, entry.second)
                }
                // Insert ListEntry rows with the quantities
                val listProducts =
                        orderedProducts.mapIndexed { i, (p, q) ->
                            ListEntry.new {
                                index = i
                                product = p
                                quantity = q
                            }
                        }
                list.products = SizedCollection(listProducts)

                println("Updated shopping list products for ${list.code}")
                return@transaction ListServiceResponse.ListResponse(list)
            }

    fun createList(entries: List<Pair<UUID, Int>>): ListServiceResponse =
            transaction {
                println("Beginning list creation transaction")
                // Find all Product matching UUID strings
                val ids = entries.map { it.first }
                val matchingProducts = Product.find { Products.id inList ids }

                println("Matching product ids ${matchingProducts.map { it.id.value.toString() }}")


                if (matchingProducts.count() != ids.size) {
                    val notFound = ids.filterNot { id -> matchingProducts.any { product -> product.id.value == id } }
                    return@transaction ListServiceResponse.ListServiceError.ProductsNotFound(notFound)
                }
                // Re-order the received products to match the ordered list sent to us
                val orderedProducts = entries.map { entry ->
                    Pair(matchingProducts.find { product -> entry.first == product.id.value }!!, entry.second)
                }
                // Insert ListEntry rows with the quantities
                val listProducts =
                        orderedProducts.mapIndexed { i, (p, q) ->
                            ListEntry.new {
                                index = i
                                product = p
                                quantity = q
                            }
                        }
                // Create the list with the ListEntries we just created
                var generatedCode: Long
                do {
                    generatedCode = Random.nextLong(1000000, 9999999)
                } while (!ShoppingList.find { ShoppingLists.code eq generatedCode }.empty())
                val list = ShoppingList.new {
                    code = generatedCode
                    time = System.currentTimeMillis()
                    products = SizedCollection(listProducts)
                }

                println("ShoppingList created ${list.id.value}. Code ${list.code}")
                return@transaction ListServiceResponse.ListResponse(list)
            }


    fun loadList(code: Long): ListServiceResponse = transaction {
        println("Loading list for code $code")
        val list = ShoppingList.find { ShoppingLists.code eq code }.limit(1).firstOrNull()
        return@transaction if (list != null) ListServiceResponse.ListResponse(list) else ListServiceResponse.ListServiceError.ListNotFound
    }

}