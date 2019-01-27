package com.sdp15.goodboi

import androidx.lifecycle.ViewModel
import androidx.annotation.NavigationRes

abstract class BaseViewModel<T> : ViewModel() {

    val transitions = SingleLiveData<@NavigationRes Int>()

    val actions = SingleLiveData<T>()

    abstract fun bind()

}