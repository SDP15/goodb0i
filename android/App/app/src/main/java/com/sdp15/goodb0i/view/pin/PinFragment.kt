package com.sdp15.goodb0i.view.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.watchText
import kotlinx.android.synthetic.main.layout_pin_input.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PinFragment : androidx.fragment.app.Fragment() {

    private val vm: PinViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_pin_input, container, false)
    }


    private fun bindViewModel() {
        vm.validInput.observe(this, Observer {
            if (it.first) {
                login_code_input.error = null
                //findNavController().navigate(R.id.device_list_fragment)
            } else {
                login_code_input.error = getString(it.second)
            }
        })
        vm.actions.observe(this, Observer {
            if (it is PinViewModel.PinAction.ConfirmShoppingListAction) {
                findNavController().navigate(PinFragmentDirections.actionPinFragmentToListConfirmationFragment(it.list))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        login_code_input.watchText(vm::onInput)
    }


    interface PinInteractor {

        fun onInput(input: String)

    }
}
