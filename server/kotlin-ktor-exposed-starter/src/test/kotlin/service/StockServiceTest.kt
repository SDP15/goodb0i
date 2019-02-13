package service

import common.ServerTest
import kotlinx.coroutines.runBlocking
import model.Stock
import org.junit.jupiter.api.Test

class StockServiceTest: ServerTest() {

    private val stockService = StockService()

//    @Test
//    fun testAddStock() = runBlocking {
//        val stock = Stock(-1, "Named item", 0.0, "", 0.0, 1, "", "", listOf(""), 0.0, "", 0.0)
//        val id = stockService.addStock(stock).id
//        val retrieved = stockService.getStock(id)
//        println("Retrieved $retrieved")
//
//    }

//    @Test
//    fun testAddStock() = runBlocking {
//        // given
//        val stock1 = NewStock(null, "stock1", 10)
//
//        // when
//        val saved = addStock(stock1)
//
//        // then
//        val retrieved = stockService.getStock(saved.id)
//        assertThat(retrieved).isEqualTo(saved)
//        assertThat(retrieved?.name).isEqualTo(stock1.name)
//        assertThat(retrieved?.quantity).isEqualTo(stock1.quantity)
//
//        Unit
//    }
//
//    @Test
//    fun testGetAllStocks() = runBlocking {
//        // given
//        val stock1 = NewStock(null, "stock1", 10)
//        val stock2 = NewStock(null, "stock2", 5)
//        addStock(stock1)
//        addStock(stock2)
//
//        // when
//        val stocks = stockService.getAllStock()
//
//        // then
//        assertThat(stocks).hasSize(2)
//        assertThat(stocks).extracting("name").containsExactlyInAnyOrder(stock1.name, stock2.name)
//        assertThat(stocks).extracting("quantity").containsExactlyInAnyOrder(stock1.quantity, stock2.quantity)
//
//        Unit
//    }
//
//    @Test
//    fun testUpdateStock() = runBlocking {
//        // given
//        val stock1 = NewStock(null, "stock1", 10)
//        val saved = addStock(stock1)
//
//        // when
//        val update = NewStock(saved.id, "updated", 46)
//        val updated = stockService.updateStock(update)
//
//        // then
//        assertThat(updated).isNotNull
//        assertThat(updated?.id).isEqualTo(update.id)
//        assertThat(updated?.name).isEqualTo(update.name)
//        assertThat(updated?.quantity).isEqualTo(update.quantity)
//
//        assertThat(stockService.getStock(saved.id)).isEqualTo(updated)
//
//        Unit
//    }
//
//    @Test
//    fun testUpdateStockNoIdInserts() = runBlocking {
//        // given
//        val stock1 = NewStock(null, "stock1", 10)
//        val inserted = stockService.updateStock(stock1)
//
//        // then
//        assertThat(inserted).isNotNull
//
//        val retrieved = stockService.getStock(inserted?.id!!)
//        assertThat(retrieved?.name).isEqualTo(stock1.name)
//        assertThat(retrieved?.quantity).isEqualTo(stock1.quantity)
//
//        Unit
//    }
//
//    @Test
//    fun testDeleteStock() = runBlocking {
//        // given
//        val stock1 = NewStock(null, "stock1", 10)
//        val saved = addStock(stock1)
//
//        // when
//        assertThat(stockService.getStock(saved.id)).isNotNull
//        val result = stockService.deleteStock(saved.id)
//
//        // then
//        assertThat(result).isTrue()
//        assertThat(stockService.getStock(saved.id)).isNull()
//        assertThat(stockService.getAllStock()).isEmpty()
//        Unit
//    }
//
//    @Nested
//    inner class NotificationCases {
//
//        @Test
//        fun testNotifyAdd() = runBlocking {
//            val stock1 = NewStock(null, "stock1", 10)
//
//            var called = false
//            val func: suspend (Notification<Stock?>) -> Unit = {
//                assertThat(it.type).isEqualTo(ChangeType.CREATE)
//                assertThat(it.entity?.name).isEqualTo(stock1.name)
//                assertThat(it.entity?.quantity).isEqualTo(stock1.quantity)
//                called = true
//            }
//
//            stockService.addChangeListener(123, func)
//            addStock(stock1)
//            assertThat(called).isTrue()
//            Unit
//        }
//
//        @Test
//        fun testNotifyUpdate() = runBlocking {
//            val stock1 = NewStock(null, "stock1", 10)
//            val saved = addStock(stock1)
//            val updated = NewStock(null, "updated", 25)
//
//            var called = false
//            val func: suspend (Notification<Stock?>) -> Unit = {
//                assertThat(it.type).isEqualTo(ChangeType.UPDATE)
//                assertThat(it.entity?.name).isEqualTo(updated.name)
//                assertThat(it.entity?.quantity).isEqualTo(updated.quantity)
//                assertThat(it.id).isEqualTo(saved.id)
//                called = true
//            }
//
//            stockService.addChangeListener(123, func)
//            stockService.updateStock(updated.copy(id=saved.id))
//            assertThat(called).isTrue()
//            Unit
//        }
//
//        @Test
//        fun testNotifyDelete() = runBlocking {
//            val stock1 = NewStock(null, "stock1", 10)
//            val saved = addStock(stock1)
//
//            var called = false
//            val func: suspend (Notification<Stock?>) -> Unit = {
//                assertThat(it.type).isEqualTo(ChangeType.DELETE)
//                assertThat(it.entity).isNull()
//                assertThat(it.id).isEqualTo(saved.id)
//                called = true
//            }
//
//            stockService.addChangeListener(123, func)
//            stockService.deleteStock(saved.id)
//            assertThat(called).isTrue()
//            Unit
//        }
//
//        @Test
//        fun testRemoveListener() = runBlocking {
//            var called = false
//            val func: suspend (Notification<Stock?>) -> Unit = {
//                called = true
//            }
//
//            stockService.addChangeListener(123, func)
//            stockService.removeChangeListener(123)
//            addStock(NewStock(null, "stock1", 10))
//            assertThat(called).isFalse()
//            Unit
//        }
//
//    }
//
//    @Nested
//    inner class ErrorCases {
//
//        @Test
//        fun testUpdateInvalidStock() = runBlocking {
//            assertThat(stockService.updateStock(NewStock(-1, "invalid", -1))).isNull()
//        }
//
//        @Test
//        fun testGetInvalidStock() = runBlocking {
//            assertThat(stockService.getStock(-1)).isNull()
//        }
//
//        @Test
//        fun testDeleteInvalidStock() = runBlocking {
//            assertThat(stockService.deleteStock(-1)).isFalse()
//            Unit
//        }
//    }
//
//    private suspend fun addStock(stock: NewStock): Stock {
//        return stockService.addStock(stock)
//    }
}