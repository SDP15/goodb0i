package controller

import common.ServerTest
import io.ktor.http.HttpStatusCode
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.products.Product
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListResourceTest : ServerTest() {

    private val stock: MutableList<Product> = mutableListOf()

    @BeforeAll
    fun loadProducts() {
        transaction {
            stock.addAll(Product.all().toList())
        }

    }

    @Test
    fun testCreateList() {
        val testList = stock.subList(0, 4).map { Pair(it.id.value.toString(), Random.nextInt(1, 10)) }
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
    fun testRetrieveList() {
        testCreateList()
        given().When().get("/lists/load/")
    }

}