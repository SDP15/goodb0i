package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.arlib.floatingsearchview.FloatingSearchView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.switchOnEmpty
import kotlinx.android.synthetic.main.layout_search.*

class SearchFragment : Fragment() {

    private val vm: ListViewModel by lazy { (parentFragment as ListPagingFragment).vm }

    override fun onResume() {
        super.onResume()
        // Specified explicitly as AS likes to autocomplete, and then later decide that it actually meant a different
        // LinearLayoutManager
        list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val adapter = ProductAdapter(vm::incrementItem, vm::decrementItem, false, {})
        list_recycler.adapter = adapter
        search_view_switcher.switchOnEmpty(adapter)

        vm.search.observe(this, Observer {
            adapter.itemsChanged(it)
        })
        val fsv = parentFragment!!.view!!.findViewById<FloatingSearchView>(R.id.floating_search_view)
        fsv.setOnQueryChangeListener(vm::onQueryChange)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Container is the ViewPager. If we ever move SearchFragment out of a ViewPager, this will crash
        return inflater.inflate(R.layout.layout_search, container, false)
    }

    interface SearchFragmentInteractor {

        fun onQueryChange(old: String, new: String)

    }

}