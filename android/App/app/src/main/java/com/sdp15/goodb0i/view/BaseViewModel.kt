package com.sdp15.goodb0i.view

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.sdp15.goodb0i.data.SingleLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.standalone.KoinComponent
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<T> : ViewModel(), KoinComponent, CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job
    /**
     * Navigation ids for fragments to navigate to
     */
    val transitions = SingleLiveData<NavDirections>()

    val actions = SingleLiveData<T>()

    abstract fun bind()

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}