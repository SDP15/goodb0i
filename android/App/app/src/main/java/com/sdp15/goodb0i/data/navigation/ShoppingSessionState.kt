package com.sdp15.goodb0i.data.navigation

sealed class ShoppingSessionState {

    // Prior to session start
    object NoSession : ShoppingSessionState()

    object Disconnected : ShoppingSessionState()

    object Connecting : ShoppingSessionState()

    object NegotiatingTrolley : ShoppingSessionState()

    object Connected : ShoppingSessionState()

}