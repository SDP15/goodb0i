package service.shopping

import service.routing.Graph
import service.routing.RouteFinder

sealed class Message {

    sealed class IncomingMessage(val body: String) : Message() {

        sealed class FromTrolley(body: String) : IncomingMessage(body) {

            class ReceivedRoute(body: String) : FromTrolley(body)

            class UserReady(body: String) : FromTrolley(body)

            class TrolleyAcceptedProduct(body: String) : FromTrolley(body)

            class TrolleyRejectedProduct(body: String) : FromTrolley(body)

            class TrolleySkippedProduct(body: String) : FromTrolley(body)

            class ReachedPoint(body: String, val id: String) : FromTrolley(body)

            class Ping(body: String) : FromTrolley(body)

            data class InvalidMessage(val message: String) : FromTrolley(message)
        }

        sealed class FromApp(body: String) : IncomingMessage(body) {

            class PlanRoute(body: String, val code: Long) : FromApp(body)

            class ReceivedRoute(body: String) : FromApp(body)

            class Reconnect(body: String, val oldId: String) : FromApp(body)

            class ProductScanned(body: String, val id: String) : FromApp(body)

            class AppRejectedProduct(body: String) : FromApp(body)

            class AppAcceptedProduct(body: String) : FromApp(body)

            class AppSkippedProduct(body: String) : FromApp(body)

            class RequestHelp(body: String) : FromApp(body)

            class RequestStop(body: String) : FromApp(body)

            class Ping(body: String) : FromApp(body)

            data class InvalidMessage(val message: String) : FromApp(message)

        }


    }

    sealed class OutgoingMessage : Message() {
        sealed class ToTrolley : OutgoingMessage() {

            data class AssignedToApp(val code: String) : ToTrolley()

            data class RouteCalculated(val route: String) : ToTrolley()

            data class AppScannedProduct(val id: String) : ToTrolley()

            object AppAcceptedProduct : ToTrolley()

            object AppRejectedProduct : ToTrolley()

            object AppSkippedProduct : ToTrolley()

            data class ConfirmMessage(val message: String) : ToTrolley()
        }

        sealed class ToApp : OutgoingMessage() {

            object TrolleyAssigned : ToApp()

            object UserReady : ToApp()

            data class Route(val route: String) : ToApp()

            data class ReachedPoint(val point: String) : ToApp()

            object TrolleyAcceptedProduct : ToApp()

            object TrolleyRejectedProduct : ToApp()

            object TrolleySkippedProduct : ToApp()
        }
    }


    object Transformer {

        private const val DELIM = "&" // Unused UTF-8 character

        fun messageFromAppString(message: String): IncomingMessage.FromApp {
            val type = message.substringBefore(DELIM)
            return when (type) {
                "Reconnect" -> IncomingMessage.FromApp.Reconnect(message, message.substringAfter(DELIM))
                "ProductScanned" -> IncomingMessage.FromApp.ProductScanned(message, message.substringAfter(DELIM))
                "AcceptedProduct" -> IncomingMessage.FromApp.AppAcceptedProduct(message)
                "RejectedProduct" -> IncomingMessage.FromApp.AppRejectedProduct(message)
                "SkippedProduct" -> IncomingMessage.FromApp.AppSkippedProduct(message)
                "RequestHelp" -> IncomingMessage.FromApp.RequestHelp(message)
                "Stop" -> IncomingMessage.FromApp.RequestStop(message)
                "ReceivedRoute" -> IncomingMessage.FromApp.ReceivedRoute(message)
                "PlanRoute" -> IncomingMessage.FromApp.PlanRoute(message, message.substringAfter(DELIM).toLong())
                "Ping" -> IncomingMessage.FromApp.Ping(message)
                else -> IncomingMessage.FromApp.InvalidMessage(message)
            }
        }

        fun messageFromTrolleyString(message: String): IncomingMessage.FromTrolley {
            val type = message.substringBefore(DELIM)
            return when (type) {
                "ReachedPoint" -> IncomingMessage.FromTrolley.ReachedPoint(message, message.substringAfter(DELIM))
                "AcceptedProduct" -> IncomingMessage.FromTrolley.TrolleyAcceptedProduct(message)
                "RejectedProduct" -> IncomingMessage.FromTrolley.TrolleyRejectedProduct(message)
                "SkippedProduct" -> IncomingMessage.FromTrolley.TrolleySkippedProduct(message)
                "UserReady" -> IncomingMessage.FromTrolley.UserReady(message)
                "ReceivedRoute" -> IncomingMessage.FromTrolley.ReceivedRoute(message)
                "Ping" -> IncomingMessage.FromTrolley.Ping(message)
                else -> IncomingMessage.FromTrolley.InvalidMessage(message)
            }
        }

        fun messageToString(message: OutgoingMessage): String = when (message) {
            is OutgoingMessage.ToApp.ReachedPoint -> "ReachedPoint$DELIM${message.point}"
            is OutgoingMessage.ToApp.TrolleyAcceptedProduct -> "TrolleyAcceptedProduct$DELIM"
            is OutgoingMessage.ToApp.TrolleyRejectedProduct -> "TrolleyRejectedProduct$DELIM"
            is OutgoingMessage.ToApp.TrolleySkippedProduct -> "TrolleySkippedProduct$DELIM"
            is OutgoingMessage.ToApp.Route -> "RouteCalculated$DELIM${message.route}"
            is OutgoingMessage.ToApp.TrolleyAssigned -> "TrolleyAssigned$DELIM"
            is OutgoingMessage.ToApp.UserReady -> "UserReady$DELIM"
            is OutgoingMessage.ToTrolley.AppAcceptedProduct -> "AppAcceptedProduct$DELIM"
            is OutgoingMessage.ToTrolley.AppRejectedProduct -> "AppRejectedProduct$DELIM"
            is OutgoingMessage.ToTrolley.AppSkippedProduct -> "AppSkippedProduct$DELIM"
            is OutgoingMessage.ToTrolley.AppScannedProduct -> "AppScannedProduct$DELIM${message.id}"
            is OutgoingMessage.ToTrolley.AssignedToApp -> "Assigned$DELIM${message.code}"
            is OutgoingMessage.ToTrolley.RouteCalculated -> "RouteCalculated$DELIM${message.route}"
            is OutgoingMessage.ToTrolley.ConfirmMessage -> "ConfirmMessage$DELIM${message.message}"
        }

        fun routeToString(route: RouteFinder.RoutingResult.Route,
                          productMap: Map<Graph.Node<Int>, List<Int>>): String {
            val builder = StringBuilder()
            val sep = ','
            val delim = '%'
            var previous = route.first()
            route.forEach { vertex ->
                if (vertex == route.first()) {
                    builder.append("start")
                } else if (vertex == route.last()) {
                    builder.append("end$delim${vertex.node.id}")
                } else {
                    // Add a turn if there's more than one way to get to the next node
                    val edges = previous.edges
                    if (edges.size > 1) {
                        val index = edges.indexOfFirst { edge -> edge.to == vertex.node }
                        println("Node $vertex Edges are $edges. Index is $index")
                        when (index) {
                            //0 -> builder.append("left")
                            0 -> builder.append("forward")
                            1 -> builder.append("right")
                        }
                        builder.append(sep)
                    }
                    if (vertex.node in productMap.keys) {
                        builder.append("stop$delim${vertex.node.id}")
                        val products = productMap[vertex.node]
                        builder.append(products?.joinToString(separator = "$delim", prefix = "$delim"))
                    } else {
                        builder.append("pass$delim${vertex.node.id}")
                    }
                }

                builder.append(sep)
                previous = vertex
            }
            builder.setLength(builder.length - 1) // Remove last comma
            return builder.toString()
        }
    }

}