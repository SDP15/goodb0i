package controller

import common.ServerTest
import io.ktor.http.HttpStatusCode
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.shelves.ShelfRack
import repository.shelves.ShelfRacks
import service.ShelfService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShelfResourceTest : ServerTest() {


    @Test
    fun testProductsForShelf() {
        val id = transaction {
            val shelfRack = ShelfRack.all().toList().random()
            shelfRack.id.value
        }
        val data = given().When()
                .get("/shelves/$id")
                .then()
                .extract()
        Assertions.assertTrue(ContentType.JSON.contentTypeStrings.any { typeString ->
            data.contentType().startsWith(typeString)
        })
    }

    @Test
    fun testNonExistentShelf() {
        given().When()
                .get("/shelves/${Int.MAX_VALUE}")
                .then()
                .statusCode(HttpStatusCode.NotFound.value)
    }

}