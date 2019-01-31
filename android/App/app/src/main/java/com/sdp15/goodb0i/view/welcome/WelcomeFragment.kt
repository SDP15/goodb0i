package com.sdp15.goodb0i.view.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.bindView
import org.koin.androidx.viewmodel.ext.android.viewModel

class WelcomeFragment : Fragment() {

    private val vm: WelcomeViewModel by viewModel()

    private val prepareOrderButton by bindView<AppCompatButton>(R.id.button_prepare_order)
    private val startShoppingButton by bindView<AppCompatButton>(R.id.button_prepare_order)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.bind()
        vm.actions.observe(this, Observer {

        })
    }

    override fun onStart() {
        super.onStart()
        prepareOrderButton.setOnClickListener { }
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