package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class AppManager(private val sessionManager: SessionManager) {

    private val members = ConcurrentHashMap<String, WebSocketSession>()
    private val listeners = ConcurrentHashMap<String, IncomingMessageListener>()


    fun addMessageListener(id: String, listener: IncomingMessageListener) {
        listeners[id] = listener
    }

    fun onMessage(id: String, messageText: String) {
        println("$id : $messageText")
        val message = Message.Transformer.messageFromAppString(messageText)
        if (message is Message.IncomingMessage.FromApp.Reconnect) {
            rejoinApp(id, message.oldId, members[id]!!)
        }
        listeners[id]?.onAppMessage(message)

    }

    suspend fun joinApp(id: String, socket: WebSocketSession) {
        if (!members.contains(id)) {
            members[id] = socket
            socket.outgoing.send(joinKey(id)) // Send code back to app
        }
    }

    fun disconnected(id: String) {
        members.remove(id)
    }

    private fun rejoinApp(id: String, oldId: String, socket: WebSocketSession) {
        println("Rejoining app $oldId with new id $id")
        listeners[id] = listeners[oldId]!!
        sessionManager.updateAppSocket(id, oldId, socket)
    }

    suspend fun removeApp(id: String, socket: WebSocketSession) {
        members.remove(id)
        //TODO: At some point in the future, remove the session data for the app
    }

    companion object {
        private const val DELIM = "&"
        private const val ID_KEY = "ID"

        fun joinKey(id: String) = Frame.Text("$ID_KEY$DELIM$id")

    }

}