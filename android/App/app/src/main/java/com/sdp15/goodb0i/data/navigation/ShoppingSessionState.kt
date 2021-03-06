package com.sdp15.goodb0i.data.navigation

import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.products.Product

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

    // Moving towards a at
    data class NavigatingTo(val from: Route.RoutePoint.IndexPoint,
                            val to: Route.RoutePoint.IndexPoint,
                            val at: Route.RoutePoint.IndexPoint,
                            val products: List<ListItem>) : ShoppingSessionState()

    /* Scanning a particular item
       NB: This state covers both ProductFragment and ScannerFragment
      */
    data class Scanning(val item: Route.RoutePoint.IndexPoint.IdentifiedPoint.Stop,
                        val toScan: List<ListItem>) : ShoppingSessionState()

    // User is being asked to confirm an item
    data class Confirming(val product: Product) : ShoppingSessionState()

    // Reached end route at
    data class Checkout(val shoppingList: ShoppingList, val products: List<ListItem>) : ShoppingSessionState()

    // User has requested help
    object AwaitingHelp : ShoppingSessionState()

}