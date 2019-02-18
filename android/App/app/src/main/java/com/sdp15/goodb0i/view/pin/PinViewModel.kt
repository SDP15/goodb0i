package com.sdp15.goodb0i.view.pin

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.lists.ListManager
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class PinViewModel : BaseViewModel<PinViewModel.PinAction>(), PinFragment.PinInteractor, KoinComponent {

    private val listManager: ListManager by inject()

    val validInput = MutableLiveData<Pair<Boolean, @StringRes Int>>()

    override fun bind() {

    }

    override fun onInput(input: String) {
        if (input.length == 7) {
            validInput.postValue(Pair(true, 0))
            GlobalScope.launch(Dispatchers.IO) {
                val result = listManager.loadList(input.toLong())
                if (result is Result.Success) {
                    Timber.i("Retrieved list ${result.data}")
                    actions.postValue(PinAction.ConfirmShoppingListAction(result.data))
                } else {
                    Timber.e("List load failure $result")
                }
            }
        } else {
            validInput.postValue(Pair(false, R.string.error_invalid_code_format))
        }
    }

    sealed class PinAction {
        data class ConfirmShoppingListAction(val list: ShoppingList): PinAction()
    }

}