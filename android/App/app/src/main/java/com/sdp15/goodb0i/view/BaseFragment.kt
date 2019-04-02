package com.sdp15.goodb0i.view

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    /**
     * Called by [com.sdp15.goodb0i.MainActivity.onBackPressed]
     * @return Whether the back press was handled by the fragment
     */
    open fun onBackPressed(): Boolean = false

    open fun onVolumeUpPressed() {}

    val baseActivity: MainActivity
        get() = activity as MainActivity


}