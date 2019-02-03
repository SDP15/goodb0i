package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.ListDiff
import kotlinx.android.synthetic.main.layout_shoppinglist.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ShoppingListFragment : Fragment() {

    lateinit var vm: ListViewModel

    override fun onResume() {
        super.onResume()
        vm = (parentFragment as ListPagingFragment).vm
        list_recycler.layoutManager = LinearLayoutManager(context)
        val adapter = ItemAdapter(vm::incrementItem, vm::decrementItem, true)
        list_recycler.adapter = adapter

        vm.list.observe(this, Observer {
            // If empty, we want to load all of the items, rather than just the last diff
            if (adapter.itemCount == 0) {
                adapter.itemsChanged(it.toAll())
            } else {
                adapter.itemsChanged(it)
            }
        })
        vm.totalPrice.observe(this, Observer {
            label_total_price.text = getString(R.string.label_item_price, it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_shoppinglist, container, false)
    }

}