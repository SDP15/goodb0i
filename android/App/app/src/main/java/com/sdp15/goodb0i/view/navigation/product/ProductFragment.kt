package com.sdp15.goodb0i.view.navigation.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_product_display.*
import org.koin.android.ext.android.inject

class ProductFragment : BaseFragment() {

    private val vm: ProductViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_product_display, container, false)
    }

    override fun onResume() {
        super.onResume()
        vm.bind()
        vm.products.observe(this, Observer { item ->
            if (item.isNotEmpty()) {
                product_text_name.text =
                    getString(R.string.label_list_item_info, item.first().quantity, item.first().product.name)
            }
            //TODO: Shelf contents

        })
        product_button_scan.setOnClickListener {
            vm.scan()
        }
        product_button_skip.setOnClickListener {
            vm.skip()
        }
        product_help_button.setOnClickListener {
            vm.requestAssistance()
        }
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }

    override fun onVolumeUpPressed() {
        super.onVolumeUpPressed()
        vm.overrideScan()
    }

    override fun onBackPressed(): Boolean = true
}