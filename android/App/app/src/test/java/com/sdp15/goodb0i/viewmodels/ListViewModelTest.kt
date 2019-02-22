package com.sdp15.goodb0i.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.data.store.products.TestDataProductLoader
import com.sdp15.goodb0i.view.ListDiff
import com.sdp15.goodb0i.view.list.creation.ListViewModel
import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.declare
import java.util.*

class ListViewModelTest : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: ListViewModel
    private lateinit var product: Product
    private lateinit var listObserver: Observer<ListDiff<ListItem>>
    private lateinit var listSlot: CapturingSlot<ListDiff<ListItem>>
    private lateinit var searchObserver: Observer<ListDiff<ListItem>>
    private lateinit var searchSlot: CapturingSlot<ListDiff<ListItem>>

    @Before
    fun setUp() {

        listObserver = mockk(relaxed = true)
        product = mockk(relaxed = true)
        listSlot = io.mockk.slot()
        searchObserver = mockk(relaxed = true)
        searchSlot = slot()
        declare { TestDataProductLoader }
        vm = ListViewModel()
        vm.bind()
        vm.list.observeForever(listObserver)
        vm.search.observeForever(searchObserver)
    }

    @Test
    fun testEmptySearch() {
        vm.search.observeForever {
            println("Observed value $it")
        }
        vm.onQueryChange("", "")
        verify {
            searchObserver.onChanged(capture(searchSlot))
        }
        println("Captured value ${searchSlot.isCaptured}")
        Assert.assertTrue("All values in search adapter should be updated", searchSlot.captured is ListDiff.All)
        Assert.assertEquals("Search results should be empty", 0, (searchSlot.captured as ListDiff.All).items.size)
    }

    @Test
    fun testIncrementItem() {

        vm.incrementItem(product)
        verify {
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("ListDiff should be add", listSlot.captured is ListDiff.Add)
        Assert.assertEquals("Same added should be returned", product, (listSlot.captured as ListDiff.Add).added.product)
        Assert.assertEquals("Count should be 1", 1, (listSlot.captured as ListDiff.Add).added.quantity)
    }

    @Test
    fun testIncrementItemTwice() {
        vm.incrementItem(product)
        vm.incrementItem(product)
        verify(exactly = 2) {
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        verify {
            searchObserver.onChanged(any())
        }
        Assert.assertTrue("ListDiff should be update", listSlot.captured is ListDiff.Update)
        Assert.assertEquals("Same added should be returned", product, (listSlot.captured as ListDiff.Update).updated.product)
        Assert.assertEquals("Count should be 2", 2, (listSlot.captured as ListDiff.Update).updated.quantity)
    }

    @Test
    fun testDecrementOnce() {
        vm.incrementItem(product)
        vm.incrementItem(product)
        vm.decrementItem(product)
        verify(exactly = 3) {
            listObserver.onChanged(any())
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        verify(exactly = 2) {
            searchObserver.onChanged(any())
        }
        Assert.assertTrue("Diff should be update", listSlot.captured is ListDiff.Update)
        Assert.assertEquals("Product should be the same", product, (listSlot.captured as ListDiff.Update).updated.product)
        Assert.assertEquals("Quantity should be 1", 1, (listSlot.captured as ListDiff.Update).updated.quantity)
    }

    @Test
    fun testDecrementToRemoval() {
        vm.incrementItem(product)
        vm.decrementItem(product)
        verify(exactly = 2) {
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("Diff should be remove", listSlot.captured is ListDiff.Remove)
        Assert.assertEquals("Product should be the same", product, (listSlot.captured as ListDiff.Remove).removed.product)
        Assert.assertFalse(
            "List should not contain added",
            listSlot.captured.items.contains((listSlot.captured as ListDiff.Remove).removed)
        )
    }

    @Test
    fun testPriceCalculation() {
        every { product.price } answers { 1.23 }
        val observer: Observer<Double> = mockk(relaxed = true)
        val slot = slot<Double>()
        vm.totalPrice.observeForever(observer)
        vm.incrementItem(product)
        verify {
            observer.onChanged(capture(slot))
        }
        Assert.assertEquals("Price should be price of single added", 1.23, slot.captured, 0.0002)
    }

    @Test
    fun testPriceOfMultipleItems() {
        val price1 = 1.23
        val price2 = 54.89
        val secondProduct: Product = mockk(relaxed = true)
        every { product.price } answers { price1 }
        every { secondProduct.price } answers { price2 }
        val id = UUID.randomUUID()
        every { secondProduct.id } answers { id.toString() }
        val priceObserver: Observer<Double> = mockk(relaxed = true)
        val slot = slot<Double>()
        vm.totalPrice.observeForever(priceObserver)
        vm.incrementItem(product)
        vm.incrementItem(product)
        vm.incrementItem(secondProduct)
        verify(exactly = 3) {
            priceObserver.onChanged(capture(slot))
        }
        Assert.assertEquals("", 2 * price1 + price2, slot.captured, 0.0002)
    }

    private fun addTestItems(count: Int): List<Product> {
        val products = (1..count).map { mockk<Product>(relaxed = true) }
        products.forEachIndexed { index, product ->
            every { product.id } answers { index.toString() }
            vm.incrementItem(product)
        }
        vm.list.observeForever(listObserver)
        clearMocks(listObserver)
        return products
    }

    @Test
    fun testSwapItemWithSelf() {
        addTestItems(10)
        vm.moveProduct(0, 0)
        verify {
            listObserver wasNot Called
        }
    }

    @Test
    fun testSwapLowToHigh() {
        val products = addTestItems(10)
        val from = 3
        val to = 1
        vm.moveProduct(from, to)
        verify(exactly = 1) {
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("Diff should be item move", listSlot.captured is ListDiff.Move)
        val diff = listSlot.captured as ListDiff.Move
        Assert.assertEquals(products[from], diff.moved.product)
        Assert.assertEquals(products[from], diff.items[to].product)
    }

    @Test
    fun testSwapHighToLow() {
        val products = addTestItems(10)
        val from = 3
        val to = 7
        vm.moveProduct(from, to)
        verify(exactly = 1) {
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("Diff should be item move", listSlot.captured is ListDiff.Move)
        val diff = listSlot.captured as ListDiff.Move
        Assert.assertEquals(products[from], diff.moved.product)
        Assert.assertEquals(products[from], diff.items[to].product)
    }

}