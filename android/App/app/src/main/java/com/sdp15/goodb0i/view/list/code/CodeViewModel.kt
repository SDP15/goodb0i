package com.sdp15.goodb0i.view.list.code

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.lists.ListManager
import com.sdp15.goodb0i.view.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.inject
import timber.log.Timber

class CodeViewModel : BaseViewModel<Any>(),
    CodeFragment.PinInteractor {

    private val listManager: ListManager by inject()

    val validInput = MutableLiveData<Pair<Boolean, @StringRes Int>>()

    override fun bind() {

    }

    override fun onInput(input: String) {
        if (input.length == 7) {
            validInput.postValue(Pair(true, 0))
            launch {
                val result = listManager.loadList(input.toLong())
                if (result is Result.Success) {
                    Timber.i("Retrieved list ${result.data}")
                    transitions.postValue(CodeFragmentDirections.actionCodeFragmentToListConfirmationFragment(result.data))
                } else {
                    //TODO: Tell the uesr that the list doesn't exist, or can't be loaded
                    Timber.e("List load failure $result")
                }
            }
        } else {
            validInput.postValue(Pair(false, R.string.error_invalid_code_format))
        }
    }


}