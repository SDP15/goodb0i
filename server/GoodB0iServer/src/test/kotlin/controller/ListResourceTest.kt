package controller

import common.ServerTest
import common.models.ShoppingList
import io.ktor.http.HttpStatusCode
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.products.Product
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListResourceTest : ServerTest() {

    private val products: MutableList<Product> = mutableListOf()


    @BeforeAll
    fun loadProducts() {
        transaction {
            products.addAll(Product.all().toList())
        }

    }

    private fun randomListBody(length: Int) = products.shuffled().take(length).map { Pair(it.id.value.toString(), Random.nextInt(1, 10)) }

    @Test
    fun testCreateList() {
        val testList = randomListBody(4)
        val response = given()
                .body(testList)
                .When()
                .contentType(ContentType.JSON)
                .post("/lists/new")
                .then()
                .statusCode(HttpStatusCode.Created.value)
                .extract()
        println("Creation response ${response.headers().asList()}\n${response.to<String>()}")
    }

    @Test
    fun testInvalidListCode() {
        given()
                .When()
                .get("/lists/load/not_a_number")
                .then()
                .statusCode(HttpStatusCode.BadRequest.value)
    }

    @Test
    fun testNonExistentCode() {
        given()
                .When()
                .get("/lists/load/0")
                .then()
                .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun testRetrieveList() {
        val testList = randomListBody(4)
        val code = given()
                .body(testList)
                .When()
                .contentType(ContentType.JSON)
                .post("/lists/new")
                .then()
                .statusCode(HttpStatusCode.Created.value)
                .extract().to<String>()
        val list = given()
                .When()
                .get("/lists/load/$code")
                .then()
                .extract().to<ShoppingList>()

        Assertions.assertEquals(code, list.code)
        println("Test ids ${testList.map { it.first }}")
        println("Actual ids ${list.products.map { it.product.id }}")
        testList.zip(list.products).forEach { (test, actual) ->
            Assertions.assertEquals(actual.product.id, test.first, "Product ids should match for")
            Assertions.assertEquals(actual.quantity, test.second, "Quantities should match")
        }
    }

    @Test
    fun testUpdateList() {
        // Test list of unique products
        val testList = randomListBody(10).toMutableList()
        val initialList = testList.subList(0, 5)
        val response = given()
                .body(initialList)
                .When()
                .contentType(ContentType.JSON)
                .post("/lists/new")
                .then()
                .statusCode(HttpStatusCode.Created.value)
                .extract()
        val updatedList = initialList
        updatedList.removeAt(0)
        updatedList[1] = updatedList[1].copy(second = 3)
        val update = given()
                .body(updatedList)
                .When()
                .contentType(ContentType.JSON)
                .post("/lists/update/${response.to<String>()}")
                .then()
                .statusCode(HttpStatusCode.OK.value)
                .extract()

    }

}