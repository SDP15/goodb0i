package com.sdp15.goodb0i.view.welcome

import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.R

class WelcomeViewModel : BaseViewModel<WelcomeViewModel.WelcomeAction>(), WelcomeFragment.WelcomeFragmentInteractor {

    override fun bind() {

    }

    override fun prepareOrder() {
        transitions.postValue(R.id.list_creation_fragment)
    }

    override fun startShopping() {
        transitions.postValue(R.id.pin_fragment)
    }


    sealed class WelcomeAction {

    }

}