package com.sdp15.goodb0i.view.list.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_list_confirmation.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ListConfirmationFragment : BaseFragment() {

    private val vm: ListConfirmationViewModel by inject()

    override fun onResume() {
        super.onResume()
        val args = navArgs<ListConfirmationFragmentArgs>()
        Timber.i("Received args $args")
        confirmation_button_edit.setOnClickListener {
            findNavController().popBackStack()
        }
        vm.price.observe(this, Observer {
            confirmation_list_cost.text = getString(R.string.label_total_price, it)
        })
        vm.code.observe(this, Observer {
            confirmation_list_code.text = getString(R.string.label_list_code, it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Container is the ViewPager. If we ever move SearchFragment out of a ViewPager, this will crash
        return inflater.inflate(R.layout.layout_list_confirmation, container, false)
    }

}