package com.sdp15.goodb0i

import androidx.lifecycle.ViewModel
import androidx.annotation.NavigationRes

abstract class BaseViewModel<T> : ViewModel() {

    val classToken = this::class

    val transitions = SingleLiveData<@NavigationRes Int>()

    val actions = SingleLiveData<T>()

    abstract fun bind()

}