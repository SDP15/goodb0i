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
                navigation_item_name.text = getString(R.string.label_navigation_to_tills)
            } else if (destination is NavigationDestination.ShelfRack) {
                navigation_item_name.text = destination.toCollect.first().product.name
                if (destination.toCollect.size > 1) {
                    // TODO: Other products on the rack
                }
                //TODO: Next item, quantity information
            }

            if (destination.distance > 1) {
                navigation_move_progress.visibility = View.VISIBLE
                navigation_move_progress.text = getString(R.string.label_navigation_progress, destination.progress, destination.distance)
            } else {
                navigation_move_progress.visibility = View.GONE
            }
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
        navigation_help_button.setOnClickListener {
            vm.requestAssistance()
        }
    }

    sealed class NavigationDestination(val distance: Int, val progress: Int) {

        class ShelfRack(distance: Int, progress: Int, val toCollect: List<ListItem>) : NavigationDestination(distance, progress)

        class EndPoint(distance: Int, progress: Int) : NavigationDestination(distance, progress)

    }

}