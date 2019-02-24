package com.sdp15.goodb0i.view.list.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_list_confirmation.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ListConfirmationFragment : BaseFragment() {

    private val vm: ListConfirmationViewModel by inject()

    override fun onResume() {
        super.onResume()
        val args = navArgs<ListConfirmationFragmentArgs>()
        Timber.i("Received args $args")
        vm.setShoppingList(args.value.shoppingList)
        confirmation_button_Pay.setOnClickListener {

        }
        vm.price.observe(this, Observer {
            confirmation_list_cost.text = getString(R.string.label_total_price, it)
        })
        vm.code.observe(this, Observer {
            confirmation_list_code.text = getString(R.string.label_list_code, it)
        })
        confirmation_button_edit.setOnClickListener {
            // If we have come from editing a fragment, but not from previous confirmation
            if (baseActivity.fragmentHistory.first == R.id.list_creation_fragment &&
                baseActivity.fragmentHistory[1] != R.id.list_confirmation_fragment
            ) {
                findNavController().navigateUp() // Navigate back to ListPagingFragment
            } else {
                findNavController().navigate(
                    ListConfirmationFragmentDirections.actionListConfirmationFragmentToListCreationFragment(
                        args.value.shoppingList
                    )
                )
            }
        }
        confirmation_button_Pay.setOnClickListener {
            vm.startNavigation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_list_confirmation, container, false)
    }

}