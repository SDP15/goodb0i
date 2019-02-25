package service.shopping

interface IncomingMessageListener {

    fun onAppMessage(message: Message.IncomingMessage.FromApp)

    fun onConnectivityChange(isConnected: Boolean)

    fun onTrolleyMessage(message: Message.IncomingMessage.FromTrolley)

}