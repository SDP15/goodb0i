package com.sdp15.goodb0i.data.navigation

sealed class Message {

    sealed class IncomingMessage : Message() {

        /**
         *  Device connected to server
         */
        data class Connected(val id: String) : IncomingMessage()

        /**
         *  Connected to trolley
         */
        class TrolleyConnected() : IncomingMessage()

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

        data class Stop(val reason: StopReason) : OutgoingMessage()

        enum class StopReason(val code: Int) {
            HelpRequest(1)
        }

    }

}