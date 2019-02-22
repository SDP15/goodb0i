package com.sdp15.goodb0i.view.welcome

import com.sdp15.goodb0i.BaseViewModel

class WelcomeViewModel : BaseViewModel<Any>(), WelcomeFragment.WelcomeFragmentInteractor {

    override fun bind() {

    }

    override fun prepareOrder() {
        transitions.postValue(WelcomeFragmentDirections.actionWelcomeFragmentToListCreationFragment())
    }

    override fun enterCode() {
        transitions.postValue(WelcomeFragmentDirections.actionWelcomeFragmentToCodeFragment())
    }

    override fun viewLists() {
        transitions.postValue(WelcomeFragmentDirections.actionWelcomeFragmentToViewShoppingList())
    }
}