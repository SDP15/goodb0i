package service

import common.ServerTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.shelves.ShelfRack

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShelfServiceTest : ServerTest() {

    private val service = ShelfService()

    @Test
    fun testLoadProductsForRack() {
        transaction {
            val shelf = ShelfRack.all().first()
            val products = shelf.shelves.map { it.product }
            val productsOnRack = service.getProductsForShelfRack(shelf.id.value)
            products.forEach {  product ->
                Assertions.assertTrue(productsOnRack.contains(product), "${product.name} should be in retrieved products")
            }

        }
    }

    @Test
    fun testLoadProductsForNonExistentRack() {
        val productsOnRack = service.getProductsForShelfRack(-1)
        Assertions.assertTrue(productsOnRack.isEmpty(), "No products should be returned for nonexistent shelf")
    }

}