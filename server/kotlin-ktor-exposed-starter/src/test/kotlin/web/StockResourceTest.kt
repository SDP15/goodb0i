package web

import common.ServerTest
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import model.Stock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StockResourceTest: ServerTest() {

//    @Test
//    fun testCreateStock() {
//        // when
//        val newStock = NewStock(null, "stock1", 12)
//        val created = addStock(newStock)
//
//        val retrieved = get("/stock/{id}", created.id)
//                .then()
//                .extract().to<Stock>()
//
//        // then
//        assertThat(created.name).isEqualTo(newStock.name)
//    }
//
//    @Test
//    fun testGetStocked() {
//        // when
//        val stock1 = NewStock(null, "stock1", 10)
//        val stock2 = NewStock(null, "stock2", 5)
//        addStock(stock1)
//        addStock(stock2)
//
//        val stocked = get("/stock")
//                .then()
//                .statusCode(200)
//                .extract().to<Lists<Stock>>()
//
//        assertThat(stocked).hasSize(2)
//        assertThat(stocked).extracting("name").containsExactlyInAnyOrder(stock1.name, stock2.name)
//        assertThat(stocked).extracting("quantity").containsExactlyInAnyOrder(stock1.quantity, stock2.quantity)
//    }
//
//    @Test
//    fun testUpdateStock() {
//        // when
//        val stock1 = NewStock(null, "stock1", 10)
//        val saved = addStock(stock1)
//
//        // then
//        val update = NewStock(saved.id, "updated", 46)
//        val updated = given()
//                .contentType(ContentType.JSON)
//                .body(update)
//                .When()
//                .put("/stock")
//                .then()
//                .statusCode(200)
//                .extract().to<Stock>()
//
//        assertThat(updated).isNotNull
//        assertThat(updated.id).isEqualTo(update.id)
//        assertThat(updated.name).isEqualTo(update.name)
//        assertThat(updated.quantity).isEqualTo(update.quantity)
//    }
//
//    @Test
//    fun testDeleteStock() {
//        // when
//        val newStock = NewStock(null, "stock1", 12)
//        val created = addStock(newStock)
//
//        // then
//        delete("/stock/{id}", created.id)
//                .then()
//                .statusCode(200)
//
//        get("/stock/{id}", created.id)
//                .then()
//                .statusCode(404)
//    }
//
//    @Nested
//    inner class ErrorCases {
//
//        @Test
//        fun testUpdateInvalidStock() {
//            val updatedStock = NewStock(-1, "invalid", -1)
//            given()
//                    .contentType(ContentType.JSON)
//                    .body(updatedStock)
//                    .When()
//                    .put("/stock")
//                    .then()
//                    .statusCode(404)
//        }
//
//        @Test
//        fun testDeleteInvalidStock() {
//            delete("/stock/{id}", "-1")
//                    .then()
//                    .statusCode(404)
//        }
//
//        @Test
//        fun testGetInvalidStock() {
//            get("/stock/{id}", "-1")
//                    .then()
//                    .statusCode(404)
//        }
//
//    }
//
//    private fun addStock(stock: NewStock): Stock {
//        return given()
//                .contentType(ContentType.JSON)
//                .body(stock)
//                .When()
//                .post("/stock")
//                .then()
//                .statusCode(201)
//                .extract().to()
//    }

}