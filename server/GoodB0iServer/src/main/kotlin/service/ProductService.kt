package service

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

    fun getProductByBarcode(barcode: String): Product? = transaction {
        Product.find { Products.gtin eq barcode }.firstOrNull()
    }

    fun search(query: String?): List<Product> = transaction {
        val filtered = Search.search(Product.all().toList(), query ?: "", { product -> listOf(product.name, product.description, product.department)}) .toList()
        println("Filtered results $filtered")
        filtered
//        Product.all().filter {
//            (it.name + it.description + it.department).toLowerCase().contains(query?.toLowerCase()
//                    ?: "")
//        }
    }

//    fun deleteProduct(code: UUID): Boolean = transaction {
//        Products.deleteWhere { Products.code eq code } > 0
//    }
}


