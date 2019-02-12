package service

import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ListService {


    fun createList(ids: List<String>, quantities: List<Int>) {
        transaction {
            println("Beginning list creation transaction")
            // Find all Stock matching UUID strings
            val matchingProducts = Stock.find { Stocks.id inList ids.map(UUID::fromString)  }
            //TODO: Some sort of error if an item does not exist
            assert(matchingProducts.count() == quantities.size)
            // Insert ListProduct rows with the quantities
            val listProducts =
            matchingProducts.zip(quantities).map {  chosen ->
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

            println("List created $list")
            list.products.forEach {
                println("ListProduct ${it.product.name}, quantity: ${it.quantity}")
            }
        }
    }

}