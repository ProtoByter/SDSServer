package me.protobyte

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import io.ktor.util.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.features.*
import io.ktor.server.engine.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.*
import io.ktor.network.tls.*
import io.ktor.utils.io.core.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlin.test.*
import io.ktor.server.testing.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import me.protobyte.plugins.*

class ApplicationTest {
    @Test
    fun testSignage() {
        withTestApplication {
            application.install(WebSockets)

            val received = arrayListOf<String>()
            application.routing {
                webSocket("/echo") {
                    try {
                        while (true) {
                            val text = (incoming.receive() as Frame.Text).readText()
                            received += text
                            outgoing.send(Frame.Text(text))
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        // Do nothing!
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }

            handleWebSocketConversation("/echo") { incoming, outgoing ->
                val textMessages = listOf("HELLO", "WORLD")
                for (msg in textMessages) {
                    outgoing.send(Frame.Text(msg))
                    assertEquals(msg, (incoming.receive() as Frame.Text).readText())
                }
                assertEquals(textMessages, received)
            }
        }
    }
}