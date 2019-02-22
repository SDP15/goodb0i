package com.sdp15.goodb0i.view

import androidx.fragment.app.Fragment
import com.sdp15.goodb0i.MainActivity

abstract class BaseFragment : Fragment() {

    /**
     * Called by [com.sdp15.goodb0i.MainActivity.onBackPressed]
     * @return Whether the back press was handled by the fragment
     */
    open fun onBackPressed(): Boolean = false

    val baseActivity: MainActivity
        get() = activity as MainActivity
}