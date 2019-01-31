package com.sdp15.goodb0i.view.pin

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.R

class PinViewModel : BaseViewModel<PinViewModel.LoginAction>(), PinFragment.PinInteractor {

    val validInput = MutableLiveData<Pair<Boolean, @StringRes Int>>()

    override fun bind() {

    }

    override fun onInput(input: String) {
        if (input.length == 10) {
            validInput.postValue(Pair(true, 0))
        } else {
            validInput.postValue(Pair(false, R.string.error_invalid_code_format))
        }
    }

    sealed class LoginAction {

    }

}