package controller.sockets

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.consumeEach
import service.shopping.AppManager
import service.shopping.SessionManager
import service.shopping.TrolleyManager

@UseExperimental(ObsoleteCoroutinesApi::class)
fun Route.sockets(sessionManager: SessionManager,
                  trolleyManager: TrolleyManager,
                  appManager: AppManager) {

    /*
    https://ktor.io/servers/features/websockets.html
    - The standard WebSocket API has the following events
        - onConnect - This happens when the webSocket body is entered
        - onTrolleyMessage - This happens in incoming.consumeEach
        - onClose - This happens when consumeEach stops
        - onError - This happens when an exception is thrown
     - Each time a device connects we:
        - Generate a unique identifier for the newly connected device
        - Register this identifier with the respective manager
        - Send the identifier back to the client device
     - Each time a message is received we:
        - Send the message and the identifier (which exists in the scope of webSocket) to the manager,
        which decides how to route the message
     */


    webSocket("/trolley") {

        println("Trolley Connected")

        val nonce = generateNonce()

        trolleyManager.joinTrolley(nonce, this)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    trolleyManager.onMessage(nonce, frame.readText())
                } else if (frame is Frame.Ping) {
                    println("Received ping from trolley")
                } else {
                    println("Received other frame from trolley $frame")
                }
            }
        } finally {
            trolleyManager.removeTrolley(nonce, this)
        }
    }

    webSocket("/app") {
        println("Socket with app opened")
        val trolley = trolleyManager.assignAvailableTrolley()
        if (trolley != null) {
            val nonce = generateNonce()
            appManager.joinApp(nonce, this)

            val session = sessionManager.createSession(nonce, this, trolley.second)
            appManager.addMessageListener(nonce, session)
            trolleyManager.addMessageListener(trolley.first, session)
            try {
                incoming.consumeEach { frame ->
                    println("Received frame $frame")
                    if (frame is Frame.Text) {
                        appManager.onMessage(nonce, frame.readText())
                    } else if (frame is Frame.Ping) {
                        println("Received ping from app")
                    } else {
                        println("Received other frame from app$frame")
                    }
                }
            } catch (e: ClosedSendChannelException) {
                appManager.disconnected(nonce)
                println("ClosedSendChannelException $e")
            } catch (e: Exception) {
                appManager.disconnected(nonce)
                e.printStackTrace()
                println("Other exception $e")
            } finally {
                println("$nonce not closed socket")
                //sessionManager.closeSession(nonce)
                //appManager.removeApp(nonce, this)
            }
        } else {
            println("No available trolley")
            outgoing.send(Frame.Text("NT&"))
            //close(CloseReason(CloseReason.Codes.TRY_AGAIN_LATER, "No available trolleys"))
        }


    }

    webSocket("/ping") {
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    outgoing.send(frame)
                }
            }
        } catch (e: Exception) {
            println("Ping exception $e")
        }
    }

    get("/check") {
        call.respond(HttpStatusCode.OK)
    }

}