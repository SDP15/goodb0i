package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.ListDiff
import kotlinx.android.synthetic.main.layout_search.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchFragment : Fragment() {

    private val vm: ListViewModel by sharedViewModel()

    private lateinit var viewPager: ViewPager

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
            adapter.itemsChanged(ListDiff.All(it))
        })
        floating_search_view.setOnQueryChangeListener(vm::onQueryChange)
        floating_search_view.setOnMenuItemClickListener {
            // Only one menu item
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