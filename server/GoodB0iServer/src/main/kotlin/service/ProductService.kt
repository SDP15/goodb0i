package service

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import repository.products.Product
import repository.products.Products
import java.util.*

class ProductService {

    fun getAllProducts(): List<Product> = transaction {
        Product.all().toList()
    }

    fun getProduct(id: String): Product? = transaction {
        Product.findById(UUID.fromString(id))
    }

    fun search(query: String?): List<Product> = transaction {
        Product.all().filter {
            (it.name + it.description + it.department).toLowerCase().contains(query?.toLowerCase()
                    ?: "")
        }
    }

//    fun deleteProduct(id: UUID): Boolean = transaction {
//        Products.deleteWhere { Products.id eq id } > 0
//    }
}

