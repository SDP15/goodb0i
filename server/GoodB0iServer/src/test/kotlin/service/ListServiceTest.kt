package service

import common.ServerTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.products.Product
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListServiceTest : ServerTest() {

    private val products = mutableListOf<Product>()

    @BeforeAll
    fun loadProducts() {
        transaction {
            products.addAll(Product.all().toList())
        }
    }

    private val service = ListService()

    @Test
    fun testLoadDefaultList() {
        service.loadList(7654321)
    }

    @Test
    fun testCreateList() {
        val contents = products.take(10).shuffled()
        val quantities = contents.map { Random.nextInt(1, 10) }
        val response = service.createList(
                contents.map {  product -> product.id.value }.zip(quantities)
        )
        Assertions.assertTrue(response is ListService.ListServiceResponse.ListResponse, "List creation should be successful")
        transaction {
            val entries = (response as ListService.ListServiceResponse.ListResponse).list.orderedProducts
            println("Sent products ${contents.map { it.id.value.toString() }}")
            println("Returned products ${entries.map { it.product.id.value.toString() }}")
            entries.zip(contents.zip(quantities)).forEach { (entry, sent) ->
                Assertions.assertEquals(sent.first.id.value, entry.product.id.value, "Response product should match sent product")
                Assertions.assertEquals(sent.second, entry.quantity, "Response quantity should match sent quantity")
            }
        }

    }

    @Test
    fun testUpdateList() {
        val contents = products.take(10).shuffled()
        val response = service.createList(
                contents.map { product ->
                    product.id.value to Random.nextInt(1, 10)
                }
        )
        Assertions.assertTrue(response is ListService.ListServiceResponse.ListResponse, "Response should be successful")
        transaction {
            val list = (response as ListService.ListServiceResponse.ListResponse).list
            val newContents = list.products.map { entry ->
                entry.product.id.value to kotlin.math.max(1, entry.quantity + Random.nextInt(-5, 5))
            }.shuffled().take(6)
            val updateResponse = service.editList(list.code, newContents)
            Assertions.assertTrue(updateResponse is ListService.ListServiceResponse.ListResponse, "Update should be successful")
            val newList = (updateResponse as ListService.ListServiceResponse.ListResponse).list
            newContents.zip(newList.products).forEach { (contents, entry) ->
                Assertions.assertEquals(contents.first, entry.product.id.value, "${contents.first} should match ${entry.product.name}")
                Assertions.assertEquals(contents.second, entry.quantity, "${contents.first} quantity should match")
            }
        }
    }

    @Test
    fun testLoadNonExistentList() {
        val response = service.loadList(-1)
        Assertions.assertTrue(response is ListService.ListServiceResponse.ListServiceError.ListNotFound, "Non existent list should not be found")
    }

    @Test
    fun testUpdateNonExistentList() {
        val updated = service.editList(-1, listOf())
        Assertions.assertTrue(updated is ListService.ListServiceResponse.ListServiceError.ListNotFound, "Update of non-existent list should fail")
    }

}