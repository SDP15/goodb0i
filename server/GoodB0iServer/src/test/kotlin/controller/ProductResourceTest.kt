package controller

import common.ServerTest
import io.restassured.RestAssured.given
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.products.Product

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductResourceTest : ServerTest() {

    private val products: MutableList<common.models.Product> = mutableListOf()

    @BeforeAll()
    fun loadProducts() {
        transaction {
            val json = gson.toJson(Product.all().toList())
            products.addAll(gson.fromJson(json, Array<common.models.Product>::class.java))
        }
    }

    @Test
    fun testLoadAll() {
        val responseProducts = given()
                .When()
                .get("/products/")
                .then()
                .extract()
                .fromJSON<Array<common.models.Product>>()
        Assertions.assertTrue(products.containsAll(responseProducts.toList()), "Received products should match DB products")

    }

    @Test
    fun testLoadById() {
        repeat(10) {
            val product = products.random()
            val responseProduct = given()
                    .When()
                    .get("/products/${product.id}")
                    .then()
                    .extract()
                    .fromJSON<common.models.Product>()
            Assertions.assertEquals(product, responseProduct)
        }
    }

    @Test
    fun testSearch() {
        repeat(10) {
            val product = products.random()
            val responseList = given()
                    .When()
                    .get("products/search/${product.name}")
                    .then()
                    .extract()
                    .fromJSON<Array<common.models.Product>>()
            Assertions.assertTrue(responseList.contains(product), "Search for name should yield product")
        }
    }

}