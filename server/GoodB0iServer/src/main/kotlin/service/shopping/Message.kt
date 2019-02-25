package service.shopping

sealed class Message {

    sealed class IncomingMessage : Message() {

        sealed class FromTrolley : IncomingMessage() {

            object UserAtTrolley : FromTrolley()

            object ReceivedRoute : FromTrolley()

            object TrolleyAcceptedProduct : FromTrolley()

            object TrolleyRejectedProduct : FromTrolley()

            data class ReachedPoint(val id: String) : FromTrolley()

            data class InvalidMessage(val message: String) : FromTrolley()
        }

        sealed class FromApp : IncomingMessage() {

            object ReceivedRoute : FromApp()

            data class Reconnect(val oldId: String) : FromApp()

            object ProductScanned : FromApp()

            object AppRejectedProduct : FromApp()

            object AppAcceptedProduct : FromApp()

            object RequestHelp : FromApp()

            object RequestStop : FromApp()

            data class InvalidMessage(val message: String) : FromApp()

        }


    }

    sealed class OutgoingMessage : Message() {
        sealed class ToTrolley : OutgoingMessage() {

            object AssignedToApp : ToTrolley()

            data class Route(val route: String) : ToTrolley()

            object AppScannedProduct : ToTrolley()

            object AppAcceptedProduct : ToTrolley()

            object AppRejectedProduct : ToTrolley()

        }

        sealed class ToApp : OutgoingMessage() {

            object TrolleyAssigned : ToApp()

            object UserAtTrolley : ToApp()

            data class Route(val route: String) : ToApp()

            data class ReachedPoint(val point: String) : ToApp()

            object TrolleyAcceptedProduct : ToApp()

            object TrolleyRejectedProduct : ToApp()
        }
    }


    object Transformer {

        private const val DELIM = "&" // Unused UTF-8 character

        fun messageFromAppString(message: String): IncomingMessage.FromApp {
            val type = message.substringBefore(DELIM)
            return when (type) {
                "RC" -> IncomingMessage.FromApp.Reconnect(message.substringAfter(DELIM))
                "PS" -> IncomingMessage.FromApp.ProductScanned
                "PA" -> IncomingMessage.FromApp.AppAcceptedProduct
                "PR" -> IncomingMessage.FromApp.AppRejectedProduct
                "RH" -> IncomingMessage.FromApp.RequestHelp
                "SP" -> IncomingMessage.FromApp.RequestStop
                "RR" -> IncomingMessage.FromApp.ReceivedRoute
                else -> IncomingMessage.FromApp.InvalidMessage(message)
            }
        }

        fun messageFromTrolleyString(message: String): IncomingMessage.FromTrolley {
            val type = message.substringBefore(DELIM)
            return when (type) {
                "RP" -> IncomingMessage.FromTrolley.ReachedPoint(message.substringAfter(DELIM))
                "PA" -> IncomingMessage.FromTrolley.TrolleyAcceptedProduct
                "PR" -> IncomingMessage.FromTrolley.TrolleyRejectedProduct
                "UT" -> IncomingMessage.FromTrolley.UserAtTrolley
                "RR" -> IncomingMessage.FromTrolley.ReceivedRoute
                else -> IncomingMessage.FromTrolley.InvalidMessage(message)
            }
        }

        fun messageToString(message: OutgoingMessage): String = when (message) {
            is OutgoingMessage.ToApp.ReachedPoint -> "RP$DELIM${message.point}"
            is OutgoingMessage.ToApp.TrolleyAcceptedProduct -> "TA"
            is OutgoingMessage.ToApp.TrolleyRejectedProduct -> "TR"
            is OutgoingMessage.ToApp.Route -> "RC$DELIM${message.route}"
            is OutgoingMessage.ToApp.TrolleyAssigned -> "TA$DELIM"
            is OutgoingMessage.ToApp.UserAtTrolley -> "UT$DELIM"
            is OutgoingMessage.ToTrolley.AppAcceptedProduct -> "AA"
            is OutgoingMessage.ToTrolley.AppRejectedProduct -> "AR"
            is OutgoingMessage.ToTrolley.AppScannedProduct -> "AS"
            is OutgoingMessage.ToTrolley.AssignedToApp -> "AA$DELIM"
            is OutgoingMessage.ToTrolley.Route -> "RC$DELIM${message.route}"
        }
    }

}