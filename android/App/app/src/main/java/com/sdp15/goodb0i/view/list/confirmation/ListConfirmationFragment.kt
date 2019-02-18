package com.sdp15.goodb0i.view.list.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class ListConfirmationFragment : BaseFragment() {

    private val vm: ListConfirmationViewModel by inject()

    override fun onResume() {
        super.onResume()
        val args = navArgs<ListConfirmationFragmentArgs>()
        Timber.i("Received args $args")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Container is the ViewPager. If we ever move SearchFragment out of a ViewPager, this will crash
        return inflater.inflate(R.layout.layout_list_confirmation, container, false)
    }

}