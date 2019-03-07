package analytics

import common.ServerTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpeedTest : ServerTest() {

//    @Test
//    fun testWebSocketPingPong() {
//        val client = HttpClient(CIO).config {
//
//            install(io.ktor.client.features.websocket.WebSockets)
//        }
//        runBlocking {
//            var count = 0
//            val start = System.currentTimeMillis()
//            client.ws(method = HttpMethod.Get, port = 8082, path = "127.0.0.1:8080/ping") { // this: DefaultClientWebSocketSession
//
//                send(Frame.Text("${count++}"))
//                while (true) {
//                    val received = incoming.receive()
//                    if (received is Frame.Text) {
//                        send(Frame.Text("${count++}"))
//                    }
//                    if ((System.currentTimeMillis() - start) > 5e9) {
//                        println("Send $count messages")
//                        send(Frame.Close())
//                        break
//                    }
//                }
//            }
//        }
//    }

}