package web

import common.ServerTest
import io.ktor.http.HttpStatusCode
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import model.Stock
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListResourceTest : ServerTest() {

    private val stock: MutableList<Stock> = mutableListOf()

    @BeforeAll
    fun loadStock() {
        transaction {
            stock.addAll(Stock.all().toList())
        }
    }

    @Test
    fun testCreateList() {
        val testList = stock.subList(0, 4).map { Pair(it.id.value.toString(), Random.nextInt(1, 10)) }
        given().contentType(ContentType.JSON)
                .body(testList)
                .When()
                .post("/lists/new")
                .then()
                .statusCode(HttpStatusCode.Created.value)
                .extract()

    }

}