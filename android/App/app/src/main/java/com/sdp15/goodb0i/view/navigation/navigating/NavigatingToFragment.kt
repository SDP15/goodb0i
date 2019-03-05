package com.sdp15.goodb0i.view.navigation.navigating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.lists.ListItem
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
        vm.destination.observe(this, Observer { destination ->
            if (destination is NavigationDestination.EndPoint) {
                //TODO
            } else if (destination is NavigationDestination.ShelfRack) {
                navigation_item_name.text = destination.toCollect.first().product.name
                if (destination.toCollect.size > 1) {
                    // TODO: Other products on the rack
                }
                //TODO: Next item, quantity information
            }
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }

    sealed class NavigationDestination {

        data class ShelfRack(val toCollect: List<ListItem>) : NavigationDestination()

        object EndPoint : NavigationDestination()

    }

}