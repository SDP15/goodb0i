package service.shopping

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import service.routing.RouteFinder

class Session(
        private val routeFinder: RouteFinder,
        private val appOut: SessionManager.AppMessageSender,
        private val trolleyOut: SessionManager.TrolleyMessageSender
) : IncomingMessageListener {


    private var trolleyReceivedRoute = false
    private var appReceivedRoute = false

    private fun plan(code: Long) {
        sendToTrolley(Message.OutgoingMessage.ToTrolley.AssignedToApp(code.toString()))
        val plan = routeFinder.plan(code)

        sendToApp(Message.OutgoingMessage.ToApp.Route(plan))
        sendToTrolley(Message.OutgoingMessage.ToTrolley.RouteCalculated(plan))
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
            }
            is Message.IncomingMessage.FromApp.RequestStop -> {
                //TODO
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