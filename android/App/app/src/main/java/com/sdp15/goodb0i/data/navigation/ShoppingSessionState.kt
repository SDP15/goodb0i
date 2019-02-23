package com.sdp15.goodb0i.data.navigation

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

    // Scanning a particular item
    data class Scanning(val item: Route.RoutePoint.EntryCollectionPoint) : ShoppingSessionState()

    // Reached end route point
    object Checkout : ShoppingSessionState()

    // User has requested help
    object AwaitingHelp : ShoppingSessionState()

}