package com.sdp15.goodb0i.view.navigation.navigating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_navigating_to.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class NavigatingToFragment : BaseFragment() {

    private val vm: NavigatingToViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_navigating_to, container, false)
    }

    override fun onResume() {
        super.onResume()
        vm.bind()
        vm.currentProduct.observe(this, Observer { item ->
            navigation_item_name.text = item.product.name
            //TODO: Next item, quantity information
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }
}