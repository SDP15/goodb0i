package service.shopping

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.h2.mvstore.ConcurrentArrayList
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
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

class Session(
        private val appOut: SessionManager.AppMessageSender,
        private val trolleyOut: SessionManager.TrolleyMessageSender
) : IncomingMessageListener, KoinComponent {

    private val routeFinder: RouteFinder by inject()
    private val listService: ListService by inject()

    private var trolleyReceivedRoute = false
    private var appReceivedRoute = false

    private var lastTrolleyPing = System.currentTimeMillis()
    private var lastAppPing = System.currentTimeMillis()

    private val receivedMessages = ArrayList<Message.IncomingMessage>()
    private var lastScannedProduct: String? = null
    private val collectedProducts = ArrayList<UUID>()

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
                            if (index == -1) null else Pair(index, shelf.position)
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
                            println("Routing error $path")
                        }
                    }
                }
            }
            is ListService.ListServiceResponse.ListServiceError -> {
                println("List service error $listResponse")
            }
        }
    }

    override fun onAppMessage(message: Message.IncomingMessage.FromApp) {
        receivedMessages += message
        lastAppPing = System.currentTimeMillis()
        println("IN: $message")
        when (message) {
            is Message.IncomingMessage.FromApp.PlanRoute -> {
                plan(message.code)
            }
            is Message.IncomingMessage.FromApp.ReceivedRoute -> {
                appReceivedRoute = true
            }
            is Message.IncomingMessage.FromApp.ProductScanned -> {
                lastScannedProduct = message.id
                sendToTrolley(Message.OutgoingMessage.ToTrolley.AppScannedProduct(message.id))
            }
            is Message.IncomingMessage.FromApp.AppAcceptedProduct -> {
                try {
                    collectedProducts.add(UUID.fromString(lastScannedProduct ?: ""))
                } catch (e: IllegalArgumentException) {
                    println("App accepted product but ID $lastScannedProduct not valid")
                }
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
        receivedMessages += message
        lastTrolleyPing = System.currentTimeMillis()
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
                try {
                    collectedProducts.add(UUID.fromString(lastScannedProduct ?: ""))
                } catch (e: IllegalArgumentException) {
                    println("Trolley accepted product but ID $lastScannedProduct not valid")
                }
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