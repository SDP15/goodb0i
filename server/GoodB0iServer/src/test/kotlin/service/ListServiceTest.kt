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
        val list = service.createList(
                contents.map { product ->
                    product.id.value.toString() to Random.nextInt(1, 10)
                }
        )
        Assertions.assertNotNull(list, "List should be created")
    }

    @Test
    fun testUpdateList() {
        val contents = products.take(10).shuffled()
        val list = service.createList(
                contents.map { product ->
                    product.id.value.toString() to Random.nextInt(1, 10)
                }
        )
        Assertions.assertNotNull(list, "List should be created")
        transaction {
            val newContents = list!!.products.map {
                it.product.id.value.toString() to kotlin.math.max(1, it.quantity + Random.nextInt(-5, 5))
            }.shuffled().take(6)
            val newList = service.editList(list.code, newContents)
            Assertions.assertNotNull(newList, "New list should not be null")
            newContents.zip(newList!!.products).forEach { (contents, entry) ->
                Assertions.assertEquals(contents.first, entry.product.id.value.toString(), "${contents.first} should match ${entry.product.name}")
                Assertions.assertEquals(contents.second, entry.quantity, "${contents.first} quantity should match")
            }
        }
    }

    @Test
    fun testLoadNonExistentList() {
        val list = service.loadList(-1)
        Assertions.assertNull(list, "Non existent list should be null")
    }

    @Test
    fun testUpdateNonExistentList() {
        val updated = service.editList(-1, listOf())
        Assertions.assertNull(updated, "Updated of non-existent list should be null")
    }

}