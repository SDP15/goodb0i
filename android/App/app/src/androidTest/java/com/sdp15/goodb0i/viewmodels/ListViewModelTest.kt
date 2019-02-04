package com.sdp15.goodb0i.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.*
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.data.store.TestDataItemLoader
import com.sdp15.goodb0i.view.ListDiff
import com.sdp15.goodb0i.view.list.ListViewModel
import com.sdp15.goodb0i.view.list.TrolleyItem
import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.declare

class ListViewModelTest : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: ListViewModel
    private lateinit var item: Item
    private lateinit var listObserver: Observer<ListDiff<TrolleyItem>>
    private lateinit var listSlot: CapturingSlot<ListDiff<TrolleyItem>>

    @Before
    fun setUp() {
//        declareMock<ItemLoader> {
//            `when`(this::search).thenReturn()
//        }
        //declareMock<ItemLoader>()
        listObserver = mockk(relaxed = true)
        item = mockk(relaxed = true)
        listSlot = io.mockk.slot()
        declare { TestDataItemLoader }
        vm = ListViewModel()
        vm.bind()
        vm.list.observeForever(listObserver)
    }

    @Test
    fun testEmptySearch() {
        val searchObserver: Observer<List<TrolleyItem>> = mockk(relaxed = true)
        val slot = slot<List<TrolleyItem>>()
        vm.searchResults.observeForever(searchObserver)
        vm.onQueryChange("", "")
        io.mockk.verify(exactly = 1) {
            searchObserver.onChanged(capture(slot))
        }
        Assert.assertEquals("Search results should be empty", 0, slot.captured.size)
    }

    @Test
    fun testIncrementItem() {

        vm.incrementItem(item)
        io.mockk.verify(exactly = 1) {
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("ListDiff should be add", listSlot.captured is ListDiff.Add)
        Assert.assertEquals("Same item should be returned", item, (listSlot.captured as ListDiff.Add).item.item)
        Assert.assertEquals("Count should be 1", 1, (listSlot.captured as ListDiff.Add).item.count)
    }

    @Test
    fun testIncrementItemTwice() {
        vm.incrementItem(item)
        vm.incrementItem(item)
        io.mockk.verify(exactly = 2) {
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("ListDiff should be update", listSlot.captured is ListDiff.Update)
        Assert.assertEquals("Same item should be returned", item, (listSlot.captured as ListDiff.Update).item.item)
        Assert.assertEquals("Count should be 2", 2, (listSlot.captured as ListDiff.Update).item.count)
    }

    @Test
    fun testDecrementOnce() {
        vm.incrementItem(item)
        vm.incrementItem(item)
        vm.decrementItem(item)
        io.mockk.verify(exactly = 3) {
            listObserver.onChanged(any())
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("Diff should be update", listSlot.captured is ListDiff.Update)
        Assert.assertEquals("Item should be the same", item, (listSlot.captured as ListDiff.Update).item.item)
        Assert.assertEquals("Quantity should be 1", 1, (listSlot.captured as ListDiff.Update).item.count)
    }

    @Test
    fun testDecrementToRemoval() {
        vm.incrementItem(item)
        vm.decrementItem(item)
        io.mockk.verify(exactly = 2) {
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("Diff should be remove", listSlot.captured is ListDiff.Remove)
        Assert.assertEquals("Item should be the same", item, (listSlot.captured as ListDiff.Remove).item.item)
        Assert.assertFalse(
            "List should not contain item",
            listSlot.captured.items.contains((listSlot.captured as ListDiff.Remove).item)
        )
    }

    @Test
    fun testPriceCalculation() {
        every { item.price } answers { 1.23 }
        val observer: Observer<Double> = mockk(relaxed = true)
        val slot = slot<Double>()
        vm.totalPrice.observeForever(observer)
        vm.incrementItem(item)
        io.mockk.verify(exactly = 1) {
            observer.onChanged(capture(slot))
        }
        Assert.assertEquals("Price should be price of single item", 1.23, slot.captured, 0.0002)
    }

    @Test
    fun testPriceOfMultipleItems() {
        val price1 = 1.23
        val price2 = 54.89
        val secondItem: Item = mockk(relaxed = true)
        every { item.price } answers { price1 }
        every { secondItem.price } answers { price2 }
        every { secondItem.id } answers { 2 }
        val observer: Observer<Double> = mockk(relaxed = true)
        val slot = slot<Double>()
        vm.totalPrice.observeForever(observer)
        vm.incrementItem(item)
        vm.incrementItem(item)
        vm.incrementItem(secondItem)
        verify(exactly = 3) {
            observer.onChanged(capture(slot))
        }
        Assert.assertEquals("",  2 * price1 + price2, slot.captured, 0.0002)
    }


}