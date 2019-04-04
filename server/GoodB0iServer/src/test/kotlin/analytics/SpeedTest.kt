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
import java.lang.Exception
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
                val messages = listOf(1, 10, 20, 30, 40, 50, 100, 200, 500, 1000, 5000, 10000).map { "a".repeat(it) }.toMutableList()
                client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 8081, path = "/ping") {
                    // this: DefaultClientWebSocketSession
                    send(Frame.Text(messages.first()))
                    for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
                        count++
                        send(if (messages.isNotEmpty()) Frame.Text(messages.first()) else Frame.Close())

                        if (count > 1000) {
                            println("Sent 1k ${messages.first().length} char messages in ${System.currentTimeMillis() - start}")
                            count = 0
                            start = System.currentTimeMillis()
                            messages.removeAt(0)
                        }
                    }
                }
            }
        }
    }

}