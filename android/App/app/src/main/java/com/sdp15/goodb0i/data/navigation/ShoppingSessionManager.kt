package com.sdp15.goodb0i.data.navigation

import kotlin.reflect.KProperty

interface ShoppingSessionManager {

    fun startSession(): ShoppingSession

    operator fun getValue(thisRef: Any?, property: KProperty<*>): ShoppingSession


    fun closeSession()

}