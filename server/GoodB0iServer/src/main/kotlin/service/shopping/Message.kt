package service.shopping

sealed class Message {

    sealed class IncomingMessage : Message() {

        sealed class FromTrolley : IncomingMessage() {

            object TrolleyAcceptedProduct : FromTrolley()

            object TrolleyRejectedProduct : FromTrolley()

            data class ReachedPoint(val id: String) : FromTrolley()

            data class InvalidMessage(val message: String) : FromTrolley()
        }

        sealed class FromApp : IncomingMessage() {

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

            object AppAcceptedProduct : ToTrolley()

            object AppRejectedProduct : ToTrolley()

        }

        sealed class ToApp : OutgoingMessage() {

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
                else -> IncomingMessage.FromApp.InvalidMessage(message)
            }
        }

        fun messageFromTrolleyString(message: String): IncomingMessage.FromTrolley {
            val type = message.substringBefore(DELIM)
            return when (type) {
                "RP" -> IncomingMessage.FromTrolley.ReachedPoint(message.substringAfter(DELIM))
                "PA" -> IncomingMessage.FromTrolley.TrolleyAcceptedProduct
                "PR" -> IncomingMessage.FromTrolley.TrolleyRejectedProduct
                else -> IncomingMessage.FromTrolley.InvalidMessage(message)
            }
        }

        fun messageToString(message: OutgoingMessage): String = when (message) {
            is OutgoingMessage.ToApp.ReachedPoint -> "RP$DELIM${message.point}"
            is OutgoingMessage.ToApp.TrolleyAcceptedProduct -> "TA"
            is OutgoingMessage.ToApp.TrolleyRejectedProduct -> "TR"
            is OutgoingMessage.ToTrolley.AppAcceptedProduct -> "AA"
            is OutgoingMessage.ToTrolley.AppRejectedProduct -> "AR"
        }
    }

}