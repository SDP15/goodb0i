package com.sdp15.goodb0i.view.navigation.connecting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
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
        vm.setShoppingList(args.value.shoppingList)
    }
}