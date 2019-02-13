package web

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.FrameType
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

fun Route.app() {

    webSocket("/connect") {
        try {
            var count = 0
            while(true) {
                val received = incoming.receive()
                println("Received frame $received with buffer ${received.buffer}")
                if (received is Frame.Text) {
                    count++
                    val text = StandardCharsets.UTF_8.decode(received.buffer).toString()
                    println("Received text message $text")
                    outgoing.send(Frame.Text("Some text from the server $count"))
                    TimeUnit.SECONDS.sleep(1)
                }
            }
        } catch (e: Throwable) {
            println("Route error $e")
        }
    }

}