package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class TrolleyManager {

    private val members = ConcurrentHashMap<String, WebSocketSession>()
    private val listeners = ConcurrentHashMap<String, TrolleyMessageListener>()

    suspend fun sendMessage(id: String, message: String) {
        members[id]?.outgoing?.send(Frame.Text(message))
    }

    suspend fun onMessage(id: String, message: String) {
        println("Trolley $id sent $message")
        members[id]?.send(Frame.Text(message + " returned"))
        //TODO Decide where to route the message
    }

    suspend fun joinTrolley(id: String, socket: WebSocketSession) {
        if (!members.contains(id)) {
            println("Trolley joined $id")
            members[id] = socket
            //TODO: Send some confirmation information
        }
    }

    suspend fun removeTrolley(id: String, socket: WebSocketSession) {
        members.remove(id)
    }

    fun setTrolleyMessageListener(id: String, listener: TrolleyMessageListener) {
        listeners[id] = listener
    }

    fun removeTrolleyMessageListener(id: String) = listeners.remove(id)

    interface TrolleyMessageListener {

        fun onTrolleyMessage(message: String)

    }

}