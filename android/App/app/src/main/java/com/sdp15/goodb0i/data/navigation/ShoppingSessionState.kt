package com.sdp15.goodb0i.data.navigation

import com.sdp15.goodb0i.data.store.lists.ListItem

/*
 * The current state of the session
 */
sealed class ShoppingSessionState {

    // Prior to session start
    object NoSession : ShoppingSessionState()

    // Connection terminated
    object Disconnected : ShoppingSessionState()

    // Initiating socket connection
    object Connecting : ShoppingSessionState()

    // Socket connected and id returned
    object Connected : ShoppingSessionState()

    // Asking server to allocate a trolley
    object NegotiatingTrolley : ShoppingSessionState()

    // Moving towards a point
    data class MovingTo(val from: Route.RoutePoint, val point: Route.RoutePoint) : ShoppingSessionState()

    /* Scanning a particular item
       NB: This state covers both ProductFragment and ScannerFragment
      */
    data class Scanning(val item: Route.RoutePoint.EntryCollectionPoint) : ShoppingSessionState()

    // User is being asked to confirm an item
    data class Confirming(val item: ListItem) : ShoppingSessionState()

    // Reached end route point
    object Checkout : ShoppingSessionState()

    // User has requested help
    object AwaitingHelp : ShoppingSessionState()

}