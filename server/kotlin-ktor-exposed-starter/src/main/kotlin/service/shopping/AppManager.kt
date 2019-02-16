package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap

class AppManager {

    private val members = ConcurrentHashMap<String, WebSocketSession>()
    private var count = 0

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
            //TODO: Send some confirmation information (the session id)
            //TODO: Lookup available trollies and link a trolley session with this session
        }
    }

    suspend fun removeApp(id: String, socket: WebSocketSession) {
        members.remove(id)
    }

}