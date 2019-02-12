package service

import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ListService {


    fun createList(ids: List<String>, quantities: List<Int>): Optional<model.List> =
        transaction {
            println("Beginning list creation transaction")
            // Find all Stock matching UUID strings
            val matchingProducts = Stock.find { Stocks.id inList ids.map(UUID::fromString) }
            //TODO: Some sort of error if an item does not exist
            assert(matchingProducts.count() == quantities.size)
            // Insert ListProduct rows with the quantities
            val listProducts =
                    matchingProducts.zip(quantities).map { chosen ->
                        ListProduct.new {
                            product = chosen.first
                            quantity = chosen.second
                        }
                    }
            // Create the list with the ListProducts we just created
            val list = model.List.new {
                code = 1
                time = System.currentTimeMillis()
                products = SizedCollection(listProducts)
            }

            println("List created ${list.id.value}. Code ${list.code}")
            list.products.forEach {
                println("ListProduct ${it.product.name}, quantity: ${it.quantity}")
            }
            return@transaction Optional.of(list)
        }

    fun loadList(code: Long): Optional<model.List> = transaction {
        println("Loading list for code $code")
        println("Existing lists . Codes are ${model.List.all().map { it.code }}")
        val found = model.List.find { Lists.code eq code }.limit(1)
        println("Found list ${found.map { it.code }}")
        if (!found.empty()) {
            return@transaction Optional.of(found.first())
        } else {
            return@transaction Optional.empty()
        }
    }

}