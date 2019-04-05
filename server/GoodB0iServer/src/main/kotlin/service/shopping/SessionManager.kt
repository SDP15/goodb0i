package service.shopping

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager : KoinComponent {
    private val sessions = ConcurrentHashMap<String, Session>()
    private val outs = ConcurrentHashMap<String, QueuedMessageSender>()

    fun createSession(id: String, appSocket: WebSocketSession, trolleySession: WebSocketSession): Session {
        val appOut = QueuedMessageSender(appSocket)
        outs[id] = appOut
        val session = Session(id, this, appOut, SimpleMessageSender(trolleySession)
        )
        sessions[id] = session
        return session
    }

    fun closeSession(id: String) {
        sessions.remove(id)
        runBlocking {
            outs[id]?.close()
            outs.remove(id)
        }
    }

    fun updateAppSocket(id: String, newId: String, newSocket: WebSocketSession) {
        outs[id]!!.updateSession(newSocket)
        outs[newId] = outs[id]!!
        outs.remove(id)
    }

    interface TrolleyMessageSender {

        suspend fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley)

        suspend fun close()

    }

    interface AppMessageSender {

        suspend fun sendToApp(message: Message.OutgoingMessage.ToApp)

        suspend fun close()

    }

    private class SimpleMessageSender(val socket: WebSocketSession) : TrolleyMessageSender {
        override suspend fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley) {
            socket.send(Frame.Text(Message.Transformer.messageToString(message)))
        }

        override suspend fun close() = socket.close(CloseReason(CloseReason.Codes.NORMAL, "Session Closed"))
    }

    private class MockTrolleySender : TrolleyMessageSender {
        override suspend fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley) {
            println("Message to trolley $message")
        }

        override suspend fun close() {}

    }

    private class QueuedMessageSender(var socket: WebSocketSession) : AppMessageSender {
        private val outGoingMessageQueue = LinkedList<String>()

        fun updateSession(newSocket: WebSocketSession) {
            socket = newSocket
        }

        override suspend fun sendToApp(message: Message.OutgoingMessage.ToApp) {
            if (socket.isActive) {
                if (outGoingMessageQueue.isNotEmpty()) {
                    outGoingMessageQueue.forEach { oldMessage ->
                        socket.send(Frame.Text(oldMessage))
                    }
                }
                socket.send(Frame.Text(Message.Transformer.messageToString(message)))
            } else {
                outGoingMessageQueue.addLast(Message.Transformer.messageToString(message))
            }
        }

        override suspend fun close() = socket.close(CloseReason(CloseReason.Codes.NORMAL, "Session Closed"))
    }


}