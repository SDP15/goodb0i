package controller.sockets

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import service.shopping.AppManager
import service.shopping.TrolleyManager
import java.nio.charset.StandardCharsets

fun Route.sockets(trolleyManager: TrolleyManager, appManager: AppManager) {

//    intercept(ApplicationCallPipeline.Features) {
//        if (call.sessions.get<SocketSession>() == null) {
//            call.sessions.set(SocketSession(generateNonce()))
//        }
//    }

    webSocket("/trolley") {

        val session = call.sessions.get<SocketSession>()

        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return@webSocket
        }
        trolleyManager.joinTrolley(session.id, this)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    //TODO: Pass to
                    trolleyManager.onMessage(session.id, StandardCharsets.UTF_8.decode(frame.buffer).toString())
                }
            }
        } finally {
            trolleyManager.removeTrolley(session.id, this)
        }
//
//        try {
//            var count = 0
//            while(true) {
//                val received = incoming.receive()
//                println("Received frame $received with buffer ${received.buffer}")
//                if (received is Frame.Text) {
//                    count++
//                    val text = StandardCharsets.UTF_8.decode(received.buffer).toString()
//                    println("Received text message $text")
//                    outgoing.send(Frame.Text("Some text from the server $count"))
//                    TimeUnit.SECONDS.sleep(1)
//                }
//            }
//        } catch (e: Throwable) {
//            println("Route error $e")
//        }
    }

    webSocket("/app") {
        val session = call.sessions.get<SocketSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return@webSocket
        }
        appManager.joinApp(session.id, this)
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    //TODO: Pass to
                    appManager.onMessage(session.id, StandardCharsets.UTF_8.decode(frame.buffer).toString())
                }
            }
        } finally {
            appManager.removeApp(session.id, this)
        }
    }

}