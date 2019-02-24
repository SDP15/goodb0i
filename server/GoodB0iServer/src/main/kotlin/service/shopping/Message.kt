package service.shopping

sealed class Message {

    sealed class IncomingMessage : Message() {

        sealed class FromTrolley : IncomingMessage() {

            object TrolleyAcceptedProduct : FromTrolley()

            object TrolleyRejectedProduct : FromTrolley()

            data class ReachedPoint(val id: String) : FromTrolley()

        }

        sealed class FromApp : IncomingMessage() {

            object AppRejectedProduct : FromApp()

            object AppAcceptedProduct : FromApp()

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

        private const val delim = "&" // Unused UTF-8 character

        fun messageFromString(message: String): IncomingMessage {

            return TODO()
        }

        fun messageToString(message: OutgoingMessage): String {
            return ""
        }


    }

}