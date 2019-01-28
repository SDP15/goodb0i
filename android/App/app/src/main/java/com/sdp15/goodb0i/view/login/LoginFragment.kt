package com.sdp15.goodb0i.view.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.watchText
import kotlinx.android.synthetic.main.layout_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LoginFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LoginFragment : androidx.fragment.app.Fragment() {

    private val vm: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_login, container, false)
    }


    private fun bindViewModel() {
        vm.validInput.observe(this, Observer {
            if (it.first) {
                login_code_input.error = null
                findNavController().navigate(R.id.scanner_fragment)
            } else {
                login_code_input.error = getString(it.second)
            }
        })
        vm.actions.observe(this, Observer {

        })



    }

    override fun onResume() {
        super.onResume()
        login_code_input.watchText(vm::onInput)
    }

    override fun onDetach() {
        super.onDetach()
    }


    interface LoginInteractor {

        fun onInput(input: String)

    }
}
