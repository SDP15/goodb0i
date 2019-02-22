package com.sdp15.goodb0i.view.welcome

import com.sdp15.goodb0i.BaseViewModel

class WelcomeViewModel : BaseViewModel<WelcomeViewModel.WelcomeAction>(), WelcomeFragment.WelcomeFragmentInteractor {

    override fun bind() {

    }

    override fun prepareOrder() {
        transitions.postValue(WelcomeFragmentDirections.actionWelcomeFragmentToListCreationFragment())
    }

    override fun startShopping() {
        transitions.postValue(WelcomeFragmentDirections.actionWelcomeFragmentToViewShoppingList())
    }


    sealed class WelcomeAction {

    }

}