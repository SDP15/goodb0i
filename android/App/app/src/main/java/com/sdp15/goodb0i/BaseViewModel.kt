package com.sdp15.goodb0i

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.sdp15.goodb0i.data.SingleLiveData

abstract class BaseViewModel<T> : ViewModel() {

    /**
     * Navigation ids for fragments to navigate to
     */
    val transitions = SingleLiveData<NavDirections>()

    val actions = SingleLiveData<T>()

    abstract fun bind()

}