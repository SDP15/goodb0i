package com.sdp15.goodb0i.view.list

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.data.store.ItemLoader
import com.sdp15.goodb0i.data.store.ListLoader
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.view.ListDiff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class ListViewModel : BaseViewModel<ListViewModel.ListAction>(), SearchFragment.SearchFragmentInteractor,
    KoinComponent {

    private val loader: ItemLoader by inject()
    private val creator: ListLoader by inject()
    // The current shopping list
    private val currentList = mutableListOf<TrolleyItem>()
    val list = MutableLiveData<ListDiff<TrolleyItem>>()

    // Most recently retrieved search results
    private val currentSearchResults = mutableListOf<TrolleyItem>()
    private val retrievedSearchResults = MutableLiveData<List<Item>>()
    // Exposed LiveData for full list changes, or updates to individual items
    val search = MediatorLiveData<ListDiff<TrolleyItem>>()

    init {
        search.addSource(list) {
            // When an item is updated in the list, post an update to that item within the current search results
            //TODO: Currently ItemAdapter checks if the item is currently visible. Should it be responsible for this?
            if (it is ListDiff.Update) search.postValue(ListDiff.Update(currentSearchResults, it.updated))
            if (it is ListDiff.Remove) search.postValue(ListDiff.Update(currentSearchResults, it.removed))
            // Add doesn't matter, as it is only called when we add a new item *from the search results*
        }
        search.addSource(retrievedSearchResults) { result ->
            currentSearchResults.clear()
            // Add each of the new items to the results, with their existing counts
            currentSearchResults.addAll(result.map { item ->
                TrolleyItem(
                    item,
                    currentList.firstOrNull { it.item.id == item.id }?.count ?: 0
                )
            })
            search.postValue(ListDiff.All(currentSearchResults))
        }
    }

    val totalPrice = MutableLiveData<Double>()

    override fun bind() {
    }

    fun onSaveList() {
        //TODO: Error handling
        GlobalScope.launch (Dispatchers.IO) {
            creator.createList(currentList.map { Pair(it.item.id, it.count) })
        }
    }

    private var searchJob: Job? = null // Ongoing search job

    override fun onQueryChange(old: String, new: String) {
        if (new.isEmpty()) {
            retrievedSearchResults.postValue(emptyList())
        } else {
            searchJob?.cancel()
            searchJob = GlobalScope.launch(Dispatchers.IO) {
                val result = loader.search(new)
                if (result is Result.Success) {
                    retrievedSearchResults.postValue(result.data)
                } else if (result is Result.Failure) {
                    //TODO: Handle search failure
                    Timber.e(result.exception, "Search failed")
                }
            }
        }
    }

    private val price: Double
        get() = currentList.sumByDouble { it.count * it.item.price }

    fun incrementItem(item: Item) {
        Timber.i("Incrementing added ${item.name}")
        val i = currentList.indexOfFirst { it.item.id == item.id }
        val diff: ListDiff<TrolleyItem>
        if (i == -1) { // Add item to list as it isn't there already
            val ci = TrolleyItem(item, 1)
            currentList.add(ci)
            diff = ListDiff.Add(currentList, ci)
        } else { // Update count and post to update adapter
            currentList[i].count++
            diff = ListDiff.Update(currentList, currentList[i])
        }
        list.postValue(diff)
        totalPrice.postValue(price)
    }

    fun decrementItem(item: Item) {
        Timber.i("Decrementing added ${item.name}")
        val ci = currentList.firstOrNull { it.item.id == item.id }
        // If the item doesn't exist in the current list, the user is decrementing an item in search which is at 0
        ci?.apply {
            count--
            if (count == 0) {
                currentList.remove(this)
                list.postValue(ListDiff.Remove(currentList, this))
            } else {
                list.postValue(ListDiff.Update(currentList, this))
            }
            totalPrice.postValue(price)
        }
    }

    sealed class ListAction {

    }

}