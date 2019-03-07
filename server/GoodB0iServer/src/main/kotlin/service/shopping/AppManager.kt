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
        /*TODO
         - It is possible that an app instance disconnects from the server while shopping
         - In this case we want to reconnect and re-establish with the server and resume the same
         shopping session
         - To do this the app can post its original session code rather than NEW_CONNECTION
         - The server can then re-link the new session with the disconnected shopping session data
         - Shopping session data should only be deleted when the app sends a disconnect message
          or after an extended period of disconnection
         */
        if (!members.contains(id)) {
            members[id] = socket
            socket.outgoing.send(joinKey(id)) // Send code back to app
        }
    }

    private fun rejoinApp(id: String, oldId: String, socket: WebSocketSession) {
        println("Rejoining app $oldId with new id $id")
        members.remove(oldId)
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