package com.sdp15.goodb0i.view.navigation.complete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_checkout_total.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CheckoutTotalFragment : BaseFragment() {

    private val vm: CheckoutViewModel by sharedViewModel(from = { parentFragment!! })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_checkout_total, container, false)
    }

    override fun onResume() {
        super.onResume()
        vm.totalPrice.observe(this, Observer { price ->
            confirmation_list_cost.text = getString(R.string.label_total_price, price)
        })
        confirmation_button_pay.setOnClickListener {
            vm.disposeSession()
        }
    }
}