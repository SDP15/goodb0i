package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class TrolleyManager {

    private val members = ConcurrentHashMap<String, WebSocketSession>()

    suspend fun sendMessage(id: String, message: String) {
        members[id]?.outgoing?.send(Frame.Text(message))
    }

    suspend fun onMessage(id: String, message: String) {
        println("Trolley $id sent $message")
        members[id]?.send(Frame.Text(message + " returnedh"))
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

}