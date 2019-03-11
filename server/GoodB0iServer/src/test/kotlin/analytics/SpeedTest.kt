package analytics

import common.ServerTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpeedTest : ServerTest() {

    @Test
    fun testWebSocketPingPong() {
        val client = HttpClient(CIO).config {

            install(io.ktor.client.features.websocket.WebSockets)
        }
        assertThrows<ClosedSendChannelException> {
            runBlocking {
                delay(Duration.ofSeconds(1))
                var count = 0
                var start = System.currentTimeMillis()
                val messages = listOf(1,10,20,30,40,50,100,200,500,1000,5000,10000).map { "a".repeat(it) }.toMutableList()
                client.ws(method = HttpMethod.Get, host = "192.168.105.36", port = 8081, path = "/ping") {
                    // this: DefaultClientWebSocketSession

                    send(Frame.Text(messages.first()))
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            count++
                            send(Frame.Text(messages.first()))
                        }
                        if (count > 10000) {
                            println("Sent 10k ${messages.first().length} char messages in ${System.currentTimeMillis() - start}")
                            count = 0
                            start = System.currentTimeMillis()
                            messages.removeAt(0)
                            if (messages.isEmpty()) close(CloseReason(CloseReason.Codes.NORMAL, ""))
                        }
                    }
                }
            }
        }
    }

}