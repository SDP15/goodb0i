package com.sdp15.goodb0i.view.navigation.complete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import org.koin.android.ext.android.inject

class CompleteFragment : BaseFragment() {

    private val vm: CompleteViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_done, container, false)
    }

    override fun onResume() {
        vm.bind()
        super.onResume()
    }
}