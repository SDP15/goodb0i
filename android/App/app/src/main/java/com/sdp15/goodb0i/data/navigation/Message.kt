package com.sdp15.goodb0i.data.navigation

import com.sdp15.goodb0i.data.navigation.sockets.SocketHandler

/*
 Incoming and Outgoing messages with the server
 */
sealed class Message {

    sealed class IncomingMessage : Message() {

        /**
         *  Device connected to server
         */
        data class Connected(val id: String) : IncomingMessage()

        /**
         *  Connected to trolley
         */
        object TrolleyConnected : IncomingMessage()

        data class RouteCalculated(val route: Route) : IncomingMessage()

        /**
         *  Trolley has reached a tag
         *  TODO: More information
         */
        data class ReachedPoint(val id: String) : IncomingMessage()

        object UserReady : IncomingMessage()


        object TrolleyAcceptedProduct : IncomingMessage()

        object TrolleyRejectedProduct : IncomingMessage()

        object TrolleySkippedProduct : IncomingMessage()

        object NoAvailableTrolley : IncomingMessage()

        /*
         Message string couldn't be parsed
         */
        data class InvalidMessage(val message: String) : IncomingMessage()

    }

    sealed class OutgoingMessage : Message() {

        data class PlanRoute(val code: Long) : OutgoingMessage()

        /**
         * Reconnect to session using old id
         */
        data class Reconnect(val oldId: String) : OutgoingMessage()

        /**
         * Product scanned by user
         */
        data class ProductScanned(val id: String) : OutgoingMessage()

        /*
         * User has accepted the product
         */
        data class AcceptedProduct(val id: String) : OutgoingMessage()

        /*
         * User has rejected the product
         */
        data class RejectedProduct(val id: String) : OutgoingMessage()

        object SkippedProduct : OutgoingMessage()

        /*
         * Request stopping the trolley
         */
        data class Stop(val reason: StopReason) : OutgoingMessage()

        object RequestHelp : OutgoingMessage()

        object ReceivedRoute : OutgoingMessage()

        enum class StopReason(val code: Int) {
            HelpRequest(1)
        }

    }

    object Transformer : SocketHandler.SocketMessageTransformer<IncomingMessage, OutgoingMessage> {

        private const val delim = "&" // Unused UTF-8 character

        override fun transformIncoming(message: String): IncomingMessage {
            val type = message.substringBefore(delim)
            return when (type) {
                "ID" -> IncomingMessage.Connected(message.substringAfter(delim))
                "TrolleyConnected" -> IncomingMessage.TrolleyConnected
                "RouteCalculated" -> {
                    val route = Route.fromString(message.substringAfter(delim))
                    if (route != null) IncomingMessage.RouteCalculated(route) else IncomingMessage.InvalidMessage(
                        message
                    )
                }
                "ReachedPoint" -> IncomingMessage.ReachedPoint(message.substringAfter(delim))
                "UserReady" -> IncomingMessage.UserReady
                "NoAvailableTrolley" -> IncomingMessage.NoAvailableTrolley
                "TrolleyAcceptedProduct" -> IncomingMessage.TrolleyAcceptedProduct
                "TrolleyRejectedProduct" -> IncomingMessage.TrolleyRejectedProduct
                "TrolleySkippedProduct" -> IncomingMessage.TrolleySkippedProduct
                else -> IncomingMessage.InvalidMessage(message)
            }
        }

        override fun transformOutgoing(message: OutgoingMessage): String {
            return when (message) {
                is OutgoingMessage.PlanRoute -> "PlanRoute$delim${message.code}"
                is OutgoingMessage.Reconnect -> "Reconnect$delim${message.oldId}"
                is OutgoingMessage.ProductScanned -> "ProductScanned$delim${message.id}"
                is OutgoingMessage.AcceptedProduct -> "AcceptedProduct$delim${message.id}"
                is OutgoingMessage.RejectedProduct -> "RejectedProduct$delim${message.id}"
                is OutgoingMessage.SkippedProduct -> "SkippedProduct$delim"
                is OutgoingMessage.RequestHelp -> "RequestHelp$delim"
                is OutgoingMessage.Stop -> "Stop$delim${message.reason.code}"
                is OutgoingMessage.ReceivedRoute -> "ReceivedRoute$delim"
            }
        }
    }

}