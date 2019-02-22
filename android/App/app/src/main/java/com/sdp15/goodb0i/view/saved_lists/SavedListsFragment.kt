package com.sdp15.goodb0i.view.saved_lists

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.AppPreferences
import kotlinx.android.synthetic.main.layout_search.*
import kotlinx.android.synthetic.main.layout_stored_lists.*


class SavedListsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.sdp15.goodb0i.R.layout.layout_stored_lists, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.init(this.requireContext())
        val orders: List<String>
        orders = AppPreferences.getOrders()
    }

    override fun onResume() {
        super.onResume()
        list_recycler2.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

    }

}
