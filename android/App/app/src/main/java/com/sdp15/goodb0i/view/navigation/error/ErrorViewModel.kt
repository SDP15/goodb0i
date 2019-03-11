package com.sdp15.goodb0i.view.navigation.error

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.navigation.sockets.SessionManager
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject

class ErrorViewModel : BaseViewModel<Any>() {

    private val sm: SessionManager by inject()

    private val errorMessage = mutableListOf<String>()
    val errorState = MutableLiveData<List<String>>()

    val errorHandled = MutableLiveData<Boolean>()

    override fun bind() {
        sm.state.observeForever(object: Observer<ShoppingSessionState> {
            override fun onChanged(state: ShoppingSessionState) {
                when (state) {
                    is ShoppingSessionState.Disconnected -> {
                        // Should be the initial error state
                        errorMessage.add("Disconnected")
                    }
                    is ShoppingSessionState.Connecting -> {
                        errorMessage.add("Connecting")
                    }
                    is ShoppingSessionState.Connected -> {
                        errorMessage.add("Connected")
                    }
                    is ShoppingSessionState.AwaitingHelp -> {
                        errorMessage.add("Awaiting help")
                    }
                    else -> {
                        //TODO: Navigate back
                        errorHandled.postValue(true)
                    }
                }
            }
        })
    }
}