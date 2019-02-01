package com.sdp15.goodb0i.view.search

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.data.store.ItemLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class SearchViewModel : BaseViewModel<SearchViewModel.SearchAction>(), SearchFragment.SearchFragmentInteractor, KoinComponent {

    private val loader: ItemLoader by inject()

    val items = MutableLiveData<List<Item>>()

    override fun bind() {
        GlobalScope.launch(Dispatchers.IO) {
            val data = loader.loadAll()
            Timber.i("Loaded items $data")
            items.postValue(data)
        }
    }

    override fun onQueryChange(old: String, new: String) {

    }

    sealed class SearchAction {

    }

}