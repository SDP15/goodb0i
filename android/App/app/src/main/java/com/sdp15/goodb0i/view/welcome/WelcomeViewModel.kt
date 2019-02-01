package com.sdp15.goodb0i.view.welcome

import androidx.annotation.IdRes
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.R

class WelcomeViewModel : BaseViewModel<WelcomeViewModel.WelcomeAction>(), WelcomeFragment.WelcomeFragmentInteractor {

    override fun bind() {

    }

    override fun prepareOrder() {
<<<<<<< HEAD
        actions.postValue(WelcomeAction.Navigate(R.id.search_fragment))
    }

    override fun startShopping() {
        actions.postValue(WelcomeAction.Navigate(R.id.pin_fragment))
=======
        transitions.postValue(R.id.search_fragment)
    }

    override fun startShopping() {
        transitions.postValue(R.id.pin_fragment)
>>>>>>> b0a0dfe4effe54d8e76fedc16942ef9a198bc96e
    }

    sealed class WelcomeAction {

    }

}