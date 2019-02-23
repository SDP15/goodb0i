package com.sdp15.goodb0i.data.navigation

import com.sdp15.goodb0i.data.navigation.sockets.SocketHandler

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

        /**
         *  Trolley has begun moving
         */
        data class MovementBegun(val type: Movement) : IncomingMessage()

        enum class Movement {
            LINEAR, TURN
        }

        data class InvalidMessage(val message: String) : IncomingMessage()

    }

    sealed class OutgoingMessage : Message() {

        /**
         * Reconnect to session using old id
         */
        data class Reconnect(val oldId: String) : OutgoingMessage()

        /**
         * Product scanned by user
         */
        data class ProductScanned(val id: String) : OutgoingMessage()

        data class ProductAccepted(val id: Long) : OutgoingMessage()

        data class ProductRejected(val id: Long) : OutgoingMessage()

        data class Stop(val reason: StopReason) : OutgoingMessage()

        object RequestHelp : OutgoingMessage()

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
                "TC" -> IncomingMessage.TrolleyConnected
                "P" -> IncomingMessage.ReachedPoint(message.substringAfter(delim))
                else -> IncomingMessage.InvalidMessage(message)
            }
        }

        override fun transformOutgoing(message: OutgoingMessage): String {
            return when (message) {
                is OutgoingMessage.Reconnect -> "RC$delim${message.oldId}"
                is OutgoingMessage.ProductScanned -> "PC$delim${message.id}"
                is OutgoingMessage.ProductAccepted -> "PA$delim${message.id}"
                is OutgoingMessage.ProductRejected -> "PR$delim${message.id}"
                is OutgoingMessage.RequestHelp -> "RH$delim"
                else -> "INVLD$delim"
            }
        }
    }

}