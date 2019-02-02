package com.sdp15.goodb0i.view

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    open fun onBackPressed(): Boolean = false

}