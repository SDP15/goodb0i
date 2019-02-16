package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap

class AppManager {

    private val members = ConcurrentHashMap<String, WebSocketSession>()
    private val sessionData = ConcurrentHashMap<String, AppSession>()
    private var count = 0

    //TODO: Decide what we actually need to store
    data class AppSession(val data: String)

    suspend fun onMessage(id: String, message: String) {
        println("$id : $message")
        members[id]?.outgoing?.apply {
            if (!isClosedForSend) {
                offer(Frame.Text("Received ${count++}"))
            }
        }
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
            sessionData[id] = AppSession("TEST")
        }
    }

    suspend fun rejoinApp(id: String, oldId: String) {
        sessionData[id] = sessionData[oldId]!! //TODO: Proper data
    }

    suspend fun removeApp(id: String, socket: WebSocketSession) {
        members.remove(id)
        //TODO: At some point in the future, remove the session data for the app
    }

    companion object {
        private const val DELIM = "$"
        private const val ID_KEY = "ID"


        fun joinKey(id: String) = Frame.Text("$ID_KEY$DELIM$id")


    }

}