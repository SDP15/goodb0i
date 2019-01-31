package com.sdp15.goodb0i.view.welcome

import androidx.annotation.IdRes
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.R

class WelcomeViewModel : BaseViewModel<WelcomeViewModel.WelcomeAction>(), WelcomeFragment.WelcomeFragmentInteractor {

    override fun bind() {

    }

    override fun prepareOrder() {
        actions.postValue(WelcomeAction.Navigate(R.id.pin_fragment))
    }

    override fun startShopping() {
        actions.postValue(WelcomeAction.Navigate(R.id.pin_fragment))
    }

    sealed class WelcomeAction {
        data class Navigate(@IdRes val destination: Int) : WelcomeAction()
    }

}