package controller.sockets

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.consumeEach
import service.shopping.AppManager
import service.shopping.TrolleyManager
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@UseExperimental(ObsoleteCoroutinesApi::class)
fun Route.sockets(trolleyManager: TrolleyManager, appManager: AppManager) {

    /*
    https://ktor.io/servers/features/websockets.html
    - The standard WebSocket API has the following events
        - onConnect - This happens when the webSocket body is entered
        - onMessage - This happens in incoming.consumeEach
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
        
        val nonce = generateNonce()
        
        trolleyManager.joinTrolley(nonce, this)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    trolleyManager.onMessage(nonce, frame.readText())
                }
            }
        } finally {
            trolleyManager.removeTrolley(nonce, this)
        }
    }

    webSocket("/app") {

        val nonce = generateNonce()

        appManager.joinApp(nonce, this)
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    appManager.onMessage(nonce, frame.readText())
                }
            }
        } catch (e: ClosedSendChannelException) {
            println("ClosedSendChannelException $e")
        } catch (e: Exception) {
            println("Other exception $e")
        } finally {
            println("$nonce closed socket")
            appManager.removeApp(nonce, this)
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

}