package service.shopping

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import repository.shelves.Shelf
import repository.shelves.ShelfRack
import repository.shelves.Shelves
import service.ListService
import service.routing.Graph
import service.routing.RouteFinder
import java.awt.Toolkit

class Session(
        private val appOut: SessionManager.AppMessageSender,
        private val trolleyOut: SessionManager.TrolleyMessageSender
) : IncomingMessageListener, KoinComponent {

    private val routeFinder: RouteFinder by inject()
    private val listService: ListService by inject()

    private var trolleyReceivedRoute = false
    private var appReceivedRoute = false

    private fun plan(code: Long) {
        sendToTrolley(Message.OutgoingMessage.ToTrolley.AssignedToApp(code.toString()))

        val listResponse = listService.loadList(code)
        when (listResponse) {
            is ListService.ListServiceResponse.ListResponse -> {
                val list = listResponse.list
                transaction {
                    val shelves = Shelf.find { Shelves.product inList list.products.map { it.product.id } }
                    val racks = shelves.map { shelf -> ShelfRack[shelf.rack] }.toSet()

                    val rackProductMap = racks.associate { rack ->
                        Graph.Node(rack.id.value) to rack.shelves.mapNotNull { shelf ->
                            val index = list.products.indexOfFirst { it.product == shelf.product }
                            if (index == -1) null else index
                        }
                    }
                    val path = routeFinder.plan(racks.map { rack ->
                        Graph.Node(rack.id.value)
                    })
                    when (path) {
                        is RouteFinder.RoutingResult.Route -> {
                            val routeString = Message.Transformer.routeToString(path, rackProductMap)

                            sendToApp(Message.OutgoingMessage.ToApp.Route(routeString))
                            sendToTrolley(Message.OutgoingMessage.ToTrolley.RouteCalculated(routeString))
                        }
                        is RouteFinder.RoutingResult.RoutingError -> {

                        }
                    }
                }
            }
            is ListService.ListServiceResponse.ListServiceError -> {

            }
        }
    }

    override fun onAppMessage(message: Message.IncomingMessage.FromApp) {
        println("IN: $message")
        when (message) {
            is Message.IncomingMessage.FromApp.PlanRoute -> {
                plan(message.code)
            }
            is Message.IncomingMessage.FromApp.ReceivedRoute -> {
                appReceivedRoute = true
            }
            is Message.IncomingMessage.FromApp.ProductScanned -> {
                sendToTrolley(Message.OutgoingMessage.ToTrolley.AppScannedProduct(message.id))
            }
            is Message.IncomingMessage.FromApp.AppAcceptedProduct -> {
                sendToTrolley(Message.OutgoingMessage.ToTrolley.AppAcceptedProduct)
            }
            is Message.IncomingMessage.FromApp.AppRejectedProduct -> {
                sendToTrolley(Message.OutgoingMessage.ToTrolley.AppRejectedProduct)
            }
            is Message.IncomingMessage.FromApp.RequestHelp -> {
                //TODO
                Toolkit.getDefaultToolkit().beep()
            }
            is Message.IncomingMessage.FromApp.RequestStop -> {
                //TODO
            }
            is Message.IncomingMessage.FromApp.AppSkippedProduct -> {
                sendToTrolley(Message.OutgoingMessage.ToTrolley.AppSkippedProduct)
            }
        }
    }

    override fun onTrolleyMessage(message: Message.IncomingMessage.FromTrolley) {
        println("IN: $message")
        when (message) {
            is Message.IncomingMessage.FromTrolley.ReceivedRoute -> {
                trolleyReceivedRoute = true
            }
            is  Message.IncomingMessage.FromTrolley.UserReady -> {
                sendToApp(Message.OutgoingMessage.ToApp.UserReady)
                sendToTrolley(Message.OutgoingMessage.ToTrolley.ConfirmMessage(message.body))
            }
            is Message.IncomingMessage.FromTrolley.TrolleyAcceptedProduct -> {
                sendToApp(Message.OutgoingMessage.ToApp.TrolleyAcceptedProduct)
            }
            is Message.IncomingMessage.FromTrolley.TrolleyRejectedProduct -> {
                sendToApp(Message.OutgoingMessage.ToApp.TrolleyRejectedProduct)
            }
            is Message.IncomingMessage.FromTrolley.ReachedPoint -> {
                sendToApp(Message.OutgoingMessage.ToApp.ReachedPoint(message.id))
            }
            is Message.IncomingMessage.FromTrolley.TrolleySkippedProduct -> {
                sendToApp(Message.OutgoingMessage.ToApp.TrolleySkippedProduct)
            }
        }
    }

    override fun onConnectivityChange(isConnected: Boolean) {

    }

    private fun sendToApp(message: Message.OutgoingMessage.ToApp) {
        println("OUT: $message")
        GlobalScope.launch { appOut.sendToApp(message) }
    }

    private fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley) {
        println("OUT: $message")
        GlobalScope.launch { trolleyOut.sendToTrolley(message) }
    }

}