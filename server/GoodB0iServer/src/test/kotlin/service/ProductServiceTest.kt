package service

import common.ServerTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.products.Product

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductServiceTest  : ServerTest() {

    private val products = mutableListOf<Product>()
    private val service = ProductService()

    @BeforeAll
    fun loadProducts() {
        transaction {
            products.addAll(Product.all().toList())
        }
    }

    @Test
    fun testGetAllProducts() {
        //TODO: Is there something we can actually test here?
    }

    @Test
    fun testGetProduct() {
        repeat(10) {
            val product = products.random()
            transaction {
                val retrieved = service.getProduct(product.id.value.toString())
                Assertions.assertEquals(product.id, retrieved?.id, "Retrieving product by code should return same product")
            }
        }
    }

    @Test
    fun testSearchByName() {
        repeat(10) {
            val product = products.random()
            transaction {
                val results = service.search(product.name)
                Assertions.assertTrue(results.any { it.id == product.id }, "Search for name should yield product")
            }
        }
    }

    @Test
    fun testSearchByDescription() {
        repeat(10) {
            val product = products.random()
            transaction {
                val results = service.search(product.description)
                Assertions.assertTrue(results.any { it.id == product.id }, "Search for name should yield product")
            }
        }
    }

}