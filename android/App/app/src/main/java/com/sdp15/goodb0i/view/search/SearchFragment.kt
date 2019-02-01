package com.sdp15.goodb0i.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_search.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SearchFragment : Fragment() {

    private val vm: SearchViewModel by viewModel()
    private val adapter = ItemAdapter()

    override fun onStart() {
        super.onStart()
        vm.bind()
    }


    override fun onResume() {
        super.onResume()
        list_recycler.layoutManager = LinearLayoutManager(context)
        list_recycler.adapter = adapter
        vm.items.observe(this, Observer {
            adapter.items = it
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_search, container, false)
    }

}