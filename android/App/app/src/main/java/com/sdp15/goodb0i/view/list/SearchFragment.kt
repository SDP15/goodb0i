package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.switchOnEmpty
import kotlinx.android.synthetic.main.layout_search.*

class SearchFragment : Fragment() {

    private val vm: ListViewModel by lazy { (parentFragment as ListPagingFragment).vm }
    private lateinit var viewPager: ViewPager

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

        floating_search_view.setOnQueryChangeListener(vm::onQueryChange)
        floating_search_view.setOnMenuItemClickListener {
            // Only one menu added
            viewPager.setCurrentItem(1, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Container is the ViewPager. If we ever move SearchFragment out of a ViewPager, this will crash
        viewPager = container as ViewPager
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_search, container, false)
    }

    interface SearchFragmentInteractor {

        fun onQueryChange(old: String, new: String)

    }

}