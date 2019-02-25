package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class AppManager {

    private val members = ConcurrentHashMap<String, WebSocketSession>()
    private val listeners = ConcurrentHashMap<String, IncomingMessageListener>()
    private var count = 0


    fun addMessageListener(id: String, listener: IncomingMessageListener) {
        listeners[id] = listener
    }

    suspend fun onMessage(id: String, message: String) {
        println("$id : $message")
        listeners[id]?.onAppMessage(Message.Transformer.messageFromAppString(message))

    }

    suspend fun joinApp(id: String, socket: WebSocketSession) {
        /*TODO
         - It is possible that an app instance disconnects from the server while shopping
         - In this case we want to reconnect and re-establish with the server and resume the same
         shopping session
         - To do this the app can post its original session id rather than NEW_CONNECTION
         - The server can then re-link the new session with the disconnected shopping session data
         - Shopping session data should only be deleted when the app sends a disconnect message
          or after an extended period of disconnection
         */
        if (!members.contains(id)) {
            members[id] = socket
            socket.outgoing.send(joinKey(id)) // Send id back to app
        }
    }

    suspend fun rejoinApp(id: String, oldId: String) {

    }

    suspend fun removeApp(id: String, socket: WebSocketSession) {
        members.remove(id)
        //TODO: At some point in the future, remove the session data for the app
    }

    companion object {
        private const val DELIM = "&"
        private const val ID_KEY = "ID"
        private const val TROLLEY_CONNECTED_KEY = "TC"
        private const val REACHED_POINT_KEY = "PT"
        private const val ROUTE_CALCULATED_KEY = "RC"

        fun joinKey(id: String) = Frame.Text("$ID_KEY$DELIM$id")

        fun trolleyConnected() = Frame.Text("$TROLLEY_CONNECTED_KEY$DELIM")

        fun reachedPoint(point: String) = Frame.Text("$REACHED_POINT_KEY$DELIM$point")

        fun calculatedRoute(route: String) = Frame.Text("$ROUTE_CALCULATED_KEY$DELIM$route")
    }

}