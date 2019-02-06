package com.sdp15.goodb0i.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
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
import timber.log.Timber

class ListViewModelTest : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: ListViewModel
    private lateinit var item: Item
    private lateinit var listObserver: Observer<ListDiff<TrolleyItem>>
    private lateinit var listSlot: CapturingSlot<ListDiff<TrolleyItem>>
    private lateinit var searchObserver: Observer<ListDiff<TrolleyItem>>
    private lateinit var searchSlot: CapturingSlot<ListDiff<TrolleyItem>>

    @Before
    fun setUp() {

        listObserver = mockk(relaxed = true)
        item = mockk(relaxed = true)
        listSlot = io.mockk.slot()
        searchObserver = mockk(relaxed = true)
        searchSlot = slot()
        declare { TestDataItemLoader }
        vm = ListViewModel()
        vm.bind()
        vm.list.observeForever(listObserver)
        vm.search.observeForever(searchObserver)
    }

    @Test
    fun testEmptySearch() {
        vm.search.observeForever {
            Timber.i("Observed value $it")
        }
        vm.onQueryChange("", "")
        verify {
            searchObserver.onChanged(capture(searchSlot))
        }
        Timber.i("Captured value ${searchSlot.isCaptured}")
        Assert.assertTrue("All values in search adapter should be updated", searchSlot.captured is ListDiff.All)
        Assert.assertEquals("Search results should be empty", 0, (searchSlot.captured as ListDiff.All).items.size)
    }

    @Test
    fun testIncrementItem() {

        vm.incrementItem(item)
        verify {
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("ListDiff should be add", listSlot.captured is ListDiff.Add)
        Assert.assertEquals("Same added should be returned", item, (listSlot.captured as ListDiff.Add).added.item)
        Assert.assertEquals("Count should be 1", 1, (listSlot.captured as ListDiff.Add).added.count)
    }

    @Test
    fun testIncrementItemTwice() {
        vm.incrementItem(item)
        vm.incrementItem(item)
        verifySequence {
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        verify {
            searchObserver.onChanged(any())
        }
        Assert.assertTrue("ListDiff should be update", listSlot.captured is ListDiff.Update)
        Assert.assertEquals("Same added should be returned", item, (listSlot.captured as ListDiff.Update).updated.item)
        Assert.assertEquals("Count should be 2", 2, (listSlot.captured as ListDiff.Update).updated.count)
    }

    @Test
    fun testDecrementOnce() {
        vm.incrementItem(item)
        vm.incrementItem(item)
        vm.decrementItem(item)
        verifySequence {
            listObserver.onChanged(any())
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        verify(exactly = 2) {
            searchObserver.onChanged(any())
        }
        Assert.assertTrue("Diff should be update", listSlot.captured is ListDiff.Update)
        Assert.assertEquals("Item should be the same", item, (listSlot.captured as ListDiff.Update).updated.item)
        Assert.assertEquals("Quantity should be 1", 1, (listSlot.captured as ListDiff.Update).updated.count)
    }

    @Test
    fun testDecrementToRemoval() {
        vm.incrementItem(item)
        vm.decrementItem(item)
        verifySequence {
            listObserver.onChanged(any())
            listObserver.onChanged(capture(listSlot))
        }
        Assert.assertTrue("Diff should be remove", listSlot.captured is ListDiff.Remove)
        Assert.assertEquals("Item should be the same", item, (listSlot.captured as ListDiff.Remove).removed.item)
        Assert.assertFalse(
            "List should not contain added",
            listSlot.captured.items.contains((listSlot.captured as ListDiff.Remove).removed)
        )
    }

    @Test
    fun testPriceCalculation() {
        every { item.price } answers { 1.23 }
        val observer: Observer<Double> = mockk(relaxed = true)
        val slot = slot<Double>()
        vm.totalPrice.observeForever(observer)
        vm.incrementItem(item)
        verify {
            observer.onChanged(capture(slot))
        }
        Assert.assertEquals("Price should be price of single added", 1.23, slot.captured, 0.0002)
    }

    @Test
    fun testPriceOfMultipleItems() {
        val price1 = 1.23
        val price2 = 54.89
        val secondItem: Item = mockk(relaxed = true)
        every { item.price } answers { price1 }
        every { secondItem.price } answers { price2 }
        every { secondItem.id } answers { 2 }
        val priceObserver: Observer<Double> = mockk(relaxed = true)
        val slot = slot<Double>()
        vm.totalPrice.observeForever(priceObserver)
        vm.incrementItem(item)
        vm.incrementItem(item)
        vm.incrementItem(secondItem)
        verify(exactly = 3) {
            priceObserver.onChanged(capture(slot))
        }
        Assert.assertEquals("",  2 * price1 + price2, slot.captured, 0.0002)
    }


}