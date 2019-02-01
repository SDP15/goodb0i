package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_search.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val vm: ListViewModel by sharedViewModel()

    override fun onStart() {
        super.onStart()
        vm.bind()
    }


    override fun onResume() {
        super.onResume()

        // Specified explicitly as AS likes to autocomplete, and then later decide that it actually meant a different
        // LinearLayoutManager
        list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val adapter = ItemAdapter(vm::incrementItem, vm::decrementItem)
        list_recycler.adapter = adapter
        vm.searchResults.observe(this, Observer {
            adapter.itemsChanged(ItemAdapter.ListDiff.All(it))
        })
        floating_search_view.setOnQueryChangeListener(vm::onQueryChange)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_search, container, false)
    }

    interface SearchFragmentInteractor {

        fun onQueryChange(old: String, new: String)

    }

}