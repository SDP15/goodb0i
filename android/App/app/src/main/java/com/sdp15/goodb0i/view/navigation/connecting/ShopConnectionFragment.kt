package com.sdp15.goodb0i.view.navigation.connecting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_shop_connection.*
import org.koin.android.ext.android.inject

class ShopConnectionFragment : BaseFragment() {

    private val vm: ShopConnectionViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_shop_connection, container, false)
    }

    override fun onResume() {
        super.onResume()
        val args = navArgs<ShopConnectionFragmentArgs>()
        vm.bind()
        vm.setShoppingList(args.value.shoppingList)
        vm.progress.observe(this, Observer { progress ->
            check_connecting.isChecked = progress > 0
            check_negotiating_trolley.isChecked = progress > 1
            check_confirming.isChecked = progress > 2
            if (progress == 1) {
                check_negotiating_trolley.announceForAccessibility(getString(R.string.accessibility_requesting_trolley))
            } else if (progress == 2) {
                check_confirming.announceForAccessibility(getString(R.string.accessibility_prompt_confirm_with_trolley))
            }
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }

    override fun onBackPressed(): Boolean = true

}