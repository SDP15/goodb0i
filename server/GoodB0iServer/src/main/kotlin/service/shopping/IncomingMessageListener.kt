package service.shopping

interface IncomingMessageListener {

    fun onAppMessage(message: String)

    fun onConnectivityChange(isConnected: Boolean)

    fun onTrolleyMessage(message: String)

}