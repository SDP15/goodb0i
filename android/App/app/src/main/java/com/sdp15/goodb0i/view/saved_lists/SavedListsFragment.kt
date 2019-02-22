package com.sdp15.goodb0i.view.saved_lists

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_shoppinglist.*


class SavedListsFragment : Fragment() {
    // TODO: Rename and change types of parameters


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_stored_lists, container, false)
    }

    override fun onResume() {
        super.onResume()
//        list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

    }

}
