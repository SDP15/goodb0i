package service.shopping

sealed class Message {

    sealed class IncomingMessage : Message() {

        sealed class FromTrolley : IncomingMessage() {

            object ReceivedRoute : FromTrolley()

            object UserReady : FromTrolley()

            object TrolleyAcceptedProduct : FromTrolley()

            object TrolleyRejectedProduct : FromTrolley()

            data class ReachedPoint(val id: String) : FromTrolley()

            data class InvalidMessage(val message: String) : FromTrolley()
        }

        sealed class FromApp : IncomingMessage() {

            data class PlanRoute(val code: Long) : FromApp()

            object ReceivedRoute : FromApp()

            data class Reconnect(val oldId: String) : FromApp()

            data class ProductScanned(val id: String) : FromApp()

            object AppRejectedProduct : FromApp()

            object AppAcceptedProduct : FromApp()

            object RequestHelp : FromApp()

            object RequestStop : FromApp()

            data class InvalidMessage(val message: String) : FromApp()

        }


    }

    sealed class OutgoingMessage : Message() {
        sealed class ToTrolley : OutgoingMessage() {

            data class AssignedToApp(val id: String) : ToTrolley()

            data class RouteCalculated(val route: String) : ToTrolley()

            data class AppScannedProduct(val id: String) : ToTrolley()

            object AppAcceptedProduct : ToTrolley()

            object AppRejectedProduct : ToTrolley()

        }

        sealed class ToApp : OutgoingMessage() {

            object TrolleyAssigned : ToApp()

            object UserReady : ToApp()

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
                "Reconnect" -> IncomingMessage.FromApp.Reconnect(message.substringAfter(DELIM))
                "ProductScanned" -> IncomingMessage.FromApp.ProductScanned(message.substringAfter(DELIM))
                "AcceptedProduct" -> IncomingMessage.FromApp.AppAcceptedProduct
                "RejectedProduct" -> IncomingMessage.FromApp.AppRejectedProduct
                "RequestHelp" -> IncomingMessage.FromApp.RequestHelp
                "Stop" -> IncomingMessage.FromApp.RequestStop
                "ReceivedRoute" -> IncomingMessage.FromApp.ReceivedRoute
                "PlanRoute" -> IncomingMessage.FromApp.PlanRoute(message.substringAfter(DELIM).toLong())
                else -> IncomingMessage.FromApp.InvalidMessage(message)
            }
        }

        fun messageFromTrolleyString(message: String): IncomingMessage.FromTrolley {
            val type = message.substringBefore(DELIM)
            return when (type) {
                "ReachedPoint" -> IncomingMessage.FromTrolley.ReachedPoint(message.substringAfter(DELIM))
                "AcceptedProduct" -> IncomingMessage.FromTrolley.TrolleyAcceptedProduct
                "RejectedProduct" -> IncomingMessage.FromTrolley.TrolleyRejectedProduct
                "UserReady" -> IncomingMessage.FromTrolley.UserReady
                "ReceivedRoute" -> IncomingMessage.FromTrolley.ReceivedRoute
                else -> IncomingMessage.FromTrolley.InvalidMessage(message)
            }
        }

        fun messageToString(message: OutgoingMessage): String = when (message) {
            is OutgoingMessage.ToApp.ReachedPoint -> "ReachedPoint$DELIM${message.point}"
            is OutgoingMessage.ToApp.TrolleyAcceptedProduct -> "TrolleyAcceptedProduct$DELIM"
            is OutgoingMessage.ToApp.TrolleyRejectedProduct -> "TrolleyRejectedProduct$DELIM"
            is OutgoingMessage.ToApp.Route -> "RouteCalculated$DELIM${message.route}"
            is OutgoingMessage.ToApp.TrolleyAssigned -> "TrolleyAssigned$DELIM"
            is OutgoingMessage.ToApp.UserReady -> "UserReady$DELIM"
            is OutgoingMessage.ToTrolley.AppAcceptedProduct -> "AppAcceptedProduct$DELIM"
            is OutgoingMessage.ToTrolley.AppRejectedProduct -> "AppRejectedProduct$DELIM"
            is OutgoingMessage.ToTrolley.AppScannedProduct -> "AppScannedProduct$DELIM${message.id}"
            is OutgoingMessage.ToTrolley.AssignedToApp -> "Assigned$DELIM"
            is OutgoingMessage.ToTrolley.RouteCalculated -> "RouteCalculated$DELIM${message.route}"
        }
    }

}