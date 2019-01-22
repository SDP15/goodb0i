package com.sdp15.goodboi

import androidx.annotation.ColorInt
import androidx.annotation.NavigationRes
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<T> : ViewModel() {

    val transitions = SingleLiveData<@NavigationRes Int>()

    val actions = SingleLiveData<T>()

}