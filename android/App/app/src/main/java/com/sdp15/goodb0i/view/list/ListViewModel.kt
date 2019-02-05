package com.sdp15.goodb0i.view.list

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.data.store.ItemLoader
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

    // The current shopping list
    private val currentList = mutableListOf<TrolleyItem>()
    val list = MutableLiveData<ListDiff<TrolleyItem>>()

    val searchResults = MutableLiveData<List<TrolleyItem>>()

    val totalPrice = MutableLiveData<Double>()

    override fun bind() {
    }

    private var search: Job? = null

    override fun onQueryChange(old: String, new: String) {
        if (new.isEmpty()) {
            searchResults.postValue(emptyList())
        } else {
            search?.cancel()
            search = GlobalScope.launch(Dispatchers.IO) {
                val result = loader.search(new)
                if (result is Result.Success) {
                    val merged = result.data.map { item ->
                        TrolleyItem(
                            item,
                            currentList.firstOrNull { it.item.id == item.id }?.count ?: 0
                        )
                    }
                    searchResults.postValue(merged.toList())
                } else if (result is Result.Failure) {
                    Timber.e(result.exception, "Search failed")
                }

            }
        }
    }

    private fun computePrice() = currentList.sumByDouble { it.count * it.item.price }

    fun incrementItem(item: Item) {
        Timber.i("Incrementing added ${item.name}")
        val i = currentList.indexOfFirst { it.item.id == item.id }
        val diff: ListDiff<TrolleyItem>
        if (i == -1) {
            val ci = TrolleyItem(item, 1)
            currentList.add(ci)
            diff = ListDiff.Add(currentList, ci)
        } else {
            currentList[i].count++
            diff = ListDiff.Update(currentList, currentList[i])
        }
        list.postValue(diff)
        totalPrice.postValue(computePrice())
    }

    fun decrementItem(item: Item) {
        Timber.i("Decrementing added ${item.name}")
        val ci = currentList.firstOrNull { it.item.id == item.id }
        ci?.apply {
            count--
            if (count == 0) {
                currentList.remove(this)
                list.postValue(ListDiff.Remove(currentList, this))
            } else {
                list.postValue(ListDiff.Update(currentList, this))
            }
        }
        totalPrice.postValue(computePrice())
    }

    sealed class ListAction {

    }

}