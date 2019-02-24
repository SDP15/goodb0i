package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.isActive
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager {

    private val sessions = ConcurrentHashMap<String, Session>()
    private val outs = ConcurrentHashMap<String, QueuedMessageSender>()

    fun createSession(id: String, appSocket: WebSocketSession, trolleySession: WebSocketSession): Session {
        val appOut = QueuedMessageSender(appSocket)
        outs[id] = appOut
        val session = Session(
                appOut,
                SimpleMessageSender(trolleySession)
        )
        sessions[id] = session
        return session
    }

    fun closeSession(id: String) {
        //TODO: Cleanup session data
        sessions.remove(id)
    }

    fun updateAppSocket(id: String, newId: String, newSocket: WebSocketSession) {
        outs[id]!!.updateSession(newSocket)
        outs[newId] = outs[id]!!
        outs.remove(id)
    }

    interface TrolleyMessageSender {

        suspend fun sendToTrolley(message: String)

    }

    interface AppMessageSender {

        suspend fun sendToApp(message: String)

    }

    private class SimpleMessageSender(val socket: WebSocketSession) : TrolleyMessageSender {
        override suspend fun sendToTrolley(message: String) {
            socket.send(Frame.Text(message))
        }
    }

    private class QueuedMessageSender(var socket: WebSocketSession) : AppMessageSender {
        private val outGoingMessageQueue = LinkedList<String>()

        fun updateSession(newSocket: WebSocketSession) {
            socket = newSocket
        }

        override suspend fun sendToApp(message: String) {
            if (socket.isActive) {
                if (outGoingMessageQueue.isNotEmpty()) {
                    outGoingMessageQueue.forEach { oldMessage ->
                        socket.send(Frame.Text(oldMessage))
                    }
                }
                socket.send(Frame.Text(message))
            } else {
                outGoingMessageQueue.addLast(message)
            }
        }
    }


}