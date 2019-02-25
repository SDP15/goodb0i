package com.sdp15.goodb0i.view.navigation.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_product_display.*
import org.koin.android.ext.android.inject

class ProductFragment : Fragment() {

    private val vm: ProductViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_product_display, container, false)
    }

    override fun onResume() {
        super.onResume()
        vm.product.observe(this, Observer { item ->
            product_text_name.text = getString(R.string.label_list_item_info, item.quantity, item.product.name)
            //TODO: Shelf contents

        })
        product_button_scan.setOnClickListener {
            vm.scan()
        }
        product_button_repeat.setOnClickListener {
            vm.repeat()
        }
    }
}