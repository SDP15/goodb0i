package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.ListDiff
import kotlinx.android.synthetic.main.layout_search.*
import timber.log.Timber

class SearchFragment : Fragment() {

    lateinit var vm: ListViewModel
    private lateinit var viewPager: ViewPager

    override fun onResume() {
        super.onResume()
        vm = (parentFragment as ListPagingFragment).vm
        // Specified explicitly as AS likes to autocomplete, and then later decide that it actually meant a different
        // LinearLayoutManager
        list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val adapter = ItemAdapter(vm::incrementItem, vm::decrementItem, false)
        list_recycler.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if ((adapter.itemCount == 0 && search_view_switcher.nextView.id == R.id.list_empty_text) ||
                    (adapter.itemCount > 0 && search_view_switcher.nextView.id == R.id.list_recycler)) {
                    search_view_switcher.showNext()
                }
            }
        })
        vm.searchResults.observe(this, Observer {
            Timber.i("Sending results to search adapter $it")
            adapter.itemsChanged(ListDiff.All(it))
        })
        vm.list.observe(this, Observer {
            Timber.i("Observed change $it")
            if (it is ListDiff.Update) {
                adapter.itemsChanged(it)
            } else if (it is ListDiff.Remove) {
                // Removal in ShoppingListFragment causes an update to the same search item, if visible
                adapter.itemsChanged(ListDiff.Update(it.items, it.item))
            }
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