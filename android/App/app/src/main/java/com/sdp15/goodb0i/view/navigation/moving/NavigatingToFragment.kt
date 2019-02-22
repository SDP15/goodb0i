package com.sdp15.goodb0i.view.navigation.moving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment

class NavigatingToFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_navigating_to, container, false)
    }

}