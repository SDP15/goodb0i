package service.shopping

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class Session(
        val appOut: SessionManager.AppMessageSender,
        val trolleyOut: SessionManager.TrolleyMessageSender
) : IncomingMessageListener {

    private val isPhoneConnected = AtomicBoolean(false)
    override fun onAppMessage(message: String) {

    }

    override fun onConnectivityChange(isConnected: Boolean) {
    }

    override fun onTrolleyMessage(message: String) {
    }


//override fun onConnectivityChange(isConnected: Boolean) = isPhoneConnected.set(isConnected)


    private fun sendToBoth(message: String) {
        sendToPhone(message)
        sendToTrolley(message)
    }

    private fun sendToPhone(message: String) {
        GlobalScope.launch { appOut.sendToApp(message) }
    }

    private fun sendToTrolley(message: String) {
        GlobalScope.launch { trolleyOut.sendToTrolley(message) }
    }

}