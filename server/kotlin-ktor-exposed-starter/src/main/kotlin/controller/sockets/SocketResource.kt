package controller.sockets

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
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
                    //TODO: Pass to
                    trolleyManager.onMessage(nonce, StandardCharsets.UTF_8.decode(frame.buffer).toString())
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
                    //TODO: Pass to
                    TimeUnit.SECONDS.sleep(1)
                    appManager.onMessage(nonce, StandardCharsets.UTF_8.decode(frame.buffer).toString())
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

}