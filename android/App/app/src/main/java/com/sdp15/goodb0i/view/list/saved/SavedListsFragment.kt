package com.sdp15.goodb0i.view.list.saved

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.RoomDB
import com.sdp15.goodb0i.switchOnEmpty
import kotlinx.android.synthetic.main.layout_stored_lists.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class SavedListsFragment : Fragment() {

    private val vm: SavedListsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_stored_lists, container, false)
    }



    override fun onResume() {
        super.onResume()
        vm.bind()
        val adapter = SavedListsAdapter(vm::open)
        saved_lists_viewswitcher.switchOnEmpty(adapter)
        lists_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        lists_recycler.adapter = adapter
        vm.lists.observe(this, Observer {
            adapter.setItems(it)
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }

}
