package me.protobyte.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.toList
import kotlin.collections.*

fun Application.configureRouting() {

    routing {
        authenticate("auth-signage-digest") {
            webSocket("/signage") { // websocketSession
                for (frame in incoming) {
                    val text = (frame as? Frame.Text)!!.readText()
                    when (text[0]) {
                        'a' -> {
                            val len1 = text[1].toByte().toInt()
                            val len2 = text[2].toByte().toInt()
                            val name = text.slice(2..2+len1)
                            val location = text.slice(2+len1..2+len1+len2)
                        }
                    }
                }
            }
        }

        authenticate("auth-manage-digest") {
            webSocket("/manage") {

            }
        }
    }
}
