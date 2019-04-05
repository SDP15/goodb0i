package service.shopping

import io.ktor.http.cio.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class TrolleyManager {

    private val members = ConcurrentHashMap<String, WebSocketSession>()
    private val listeners = ConcurrentHashMap<String, IncomingMessageListener>()

    fun addMessageListener(id: String, listener: IncomingMessageListener) {
        listeners[id] = listener
    }


    fun assignAvailableTrolley(): Pair<String, WebSocketSession>? {
        //TODO: Actually track assigned trolleys
        return members.entries.firstOrNull()?.toPair()
    }

    suspend fun onMessage(id: String, message: String) {
        println("Trolley $id sent $message")
        listeners[id]?.onTrolleyMessage(Message.Transformer.messageFromTrolleyString(message))
    }

    suspend fun joinTrolley(id: String, socket: WebSocketSession) {
        if (!members.contains(id)) {
            println("Trolley joined $id")
            members[id] = socket
        }
    }

    suspend fun removeTrolley(id: String, socket: WebSocketSession) {
        members.remove(id)
    }

    fun setTrolleyMessageListener(id: String, listener: IncomingMessageListener) {
        listeners[id] = listener
    }

    fun removeTrolleyMessageListener(id: String) = listeners.remove(id)

}