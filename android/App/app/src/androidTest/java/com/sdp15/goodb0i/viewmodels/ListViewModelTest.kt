package com.sdp15.goodb0i.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.runner.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import com.nhaarman.mockitokotlin2.*
import com.sdp15.goodb0i.data.store.ItemLoader
import com.sdp15.goodb0i.data.store.TestDataItemLoader
import com.sdp15.goodb0i.view.ListDiff
import com.sdp15.goodb0i.view.list.ListViewModel
import com.sdp15.goodb0i.view.list.TrolleyItem
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.declare
import org.koin.test.declareMock
import org.mockito.Mockito.`when`

class ListViewModelTest : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: ListViewModel
    private lateinit var searchCaptor: KArgumentCaptor<List<TrolleyItem>>
    private lateinit var listCaptor: KArgumentCaptor<ListDiff<TrolleyItem>>
    private lateinit var priceCaptor: KArgumentCaptor<Double>
    private lateinit var searchObserver: Observer<List<TrolleyItem>>
    private lateinit var listObserver: Observer<ListDiff<TrolleyItem>>

    @Before
    fun setUp() {
//        declareMock<ItemLoader> {
//            `when`(this::search).thenReturn()
//        }
        //declareMock<ItemLoader>()
        searchCaptor = argumentCaptor()
        listCaptor = argumentCaptor()
        priceCaptor = argumentCaptor()
        searchObserver = mock()
        listObserver = mock()
        declare { TestDataItemLoader }
        vm = ListViewModel()
        vm.bind()
    }

    @Test
    fun testEmptySearch() {

        vm.searchResults.observeForever(searchObserver)
        vm.onQueryChange("", "")
        verify(searchObserver, times(1)).onChanged(searchCaptor.capture())
        Assert.assertEquals("Search results should be empty", 0, searchCaptor.firstValue.size)
    }

    @Test
    fun testIncrementItem() {
        vm.list.observeForever(listObserver)
    }

}