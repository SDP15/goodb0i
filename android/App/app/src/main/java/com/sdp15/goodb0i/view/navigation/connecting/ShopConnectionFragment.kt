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
        vm.log.observe(this, Observer {
            shop_collection_log.text = it
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }
}