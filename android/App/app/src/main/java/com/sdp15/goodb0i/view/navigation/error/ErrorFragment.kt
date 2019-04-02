package com.sdp15.goodb0i.view.navigation.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ErrorFragment : BaseFragment() {

    private val vm: ErrorViewModel by viewModel()
    private var handled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_error, container, false)
    }

    override fun onResume() {
        super.onResume()
        vm.bind()
        vm.errorHandled.observe(this, Observer { handled ->
            this@ErrorFragment.handled = handled
            if (handled) findNavController().navigateUp()
        })
    }

    override fun onBackPressed(): Boolean {
        return !handled
    }
}