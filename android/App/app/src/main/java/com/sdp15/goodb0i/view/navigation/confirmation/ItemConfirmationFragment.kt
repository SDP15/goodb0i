package com.sdp15.goodb0i.view.navigation.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_confirmation.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ItemConfirmationFragment : BaseFragment() {

    private val vm: ItemConfirmationViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_confirmation, container, false)
    }

    override fun onResume() {
        super.onResume()
        vm.bind()
        confirmation_button_positive.setOnClickListener { vm.accept() }
        confirmation_button_negative.setOnClickListener { vm.reject() }
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
        vm.product.observe(this, Observer { product ->
            confirmation_title.text = product.name
        })
    }

    override fun onBackPressed(): Boolean = true
}