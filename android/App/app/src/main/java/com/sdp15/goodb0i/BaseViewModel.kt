package com.sdp15.goodb0i

import androidx.annotation.NavigationRes
import androidx.lifecycle.ViewModel
import com.sdp15.goodb0i.data.SingleLiveData

abstract class BaseViewModel<T> : ViewModel() {

    val classToken = this::class

    val transitions = SingleLiveData<@NavigationRes Int>()

    val actions = SingleLiveData<T>()

    abstract fun bind()

}