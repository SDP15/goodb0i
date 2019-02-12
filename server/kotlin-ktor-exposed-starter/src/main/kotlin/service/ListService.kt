package service

import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ListService {


    fun createList(ids: List<String>, quantities: List<Int>) {
        transaction {
            println("Beginning list creation transaction")
            val matchingProducts = Stock.find { Stocks.id inList ids.map(UUID::fromString)  }
            assert(matchingProducts.count() == quantities.size)
            val listProducts =
            matchingProducts.zip(quantities).map {  chosen ->
                ListProduct.new {
                    product = chosen.first
                    quantity = chosen.second
                }
            }
            val list = model.List.new(UUID.randomUUID()) {
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