package com.sdp15.goodb0i.view.navigation.complete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import com.sdp15.goodb0i.view.list.creation.ProductAdapter
import kotlinx.android.synthetic.main.layout_checkout_list.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CheckoutListFragment : BaseFragment() {

    private val vm: CheckoutViewModel by sharedViewModel(from = { parentFragment!! })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_checkout_list, container, false)
    }


    override fun onResume() {
        super.onResume()
        list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        val adapter = ProductAdapter(
            vm::incrementItem,
            vm::decrementItem,
            false,
            {}
        )
        list_recycler.adapter = adapter

        vm.products.observe(this, Observer { diff ->
            if (adapter.itemCount == 0) {
                adapter.itemsChanged(diff.toAll())
            } else {
                adapter.itemsChanged(diff)
            }
        })

    }
}