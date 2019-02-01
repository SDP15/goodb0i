package com.sdp15.goodb0i.view.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_welcome.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class WelcomeFragment : Fragment() {

    private val vm: WelcomeViewModel by viewModel()


    override fun onResume() {
        super.onResume()
        // NB: The ViewModel will immediately post an action to navigate to another fragment
        // although we could just do that here, we may later need to implement other checks in the ViewModel
        button_prepare_order.setOnClickListener {
            vm.prepareOrder() }
        button_enter_pin.setOnClickListener {
            vm.startShopping() }
        vm.bind()
        vm.actions.observe(this, Observer {
            if (it is WelcomeViewModel.WelcomeAction.Navigate) {
                findNavController().navigate(it.destination)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_welcome, container, false)
    }

    interface WelcomeFragmentInteractor {

        fun prepareOrder()

        fun startShopping()

    }

}