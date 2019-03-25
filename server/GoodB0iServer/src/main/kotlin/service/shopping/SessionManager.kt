package service.shopping

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.isActive
import service.ListService
import service.routing.GenRouteFinder
import service.routing.RouteFinder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager(private val listService: ListService, private val routeFinder: RouteFinder) {

    private val sessions = ConcurrentHashMap<String, Session>()
    private val outs = ConcurrentHashMap<String, QueuedMessageSender>()

    fun createSession(id: String, appSocket: WebSocketSession, trolleySession: WebSocketSession): Session {
        val appOut = QueuedMessageSender(appSocket)
        outs[id] = appOut
        val session = Session(
                listService,
                routeFinder,
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

        suspend fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley)

    }

    interface AppMessageSender {

        suspend fun sendToApp(message: Message.OutgoingMessage.ToApp)

    }

    private class SimpleMessageSender(val socket: WebSocketSession) : TrolleyMessageSender {
        override suspend fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley) {
            socket.send(Frame.Text(Message.Transformer.messageToString(message)))
        }
    }

    private class MockTrolleySender : TrolleyMessageSender {
        override suspend fun sendToTrolley(message: Message.OutgoingMessage.ToTrolley) {
            println("Message to trolley $message")
        }
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
    }


}