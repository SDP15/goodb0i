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

        override fun transformIncoming(message: String): IncomingMessage {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun transformOutgoing(message: OutgoingMessage): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}