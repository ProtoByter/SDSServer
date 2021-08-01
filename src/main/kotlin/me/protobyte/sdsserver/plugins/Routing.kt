package me.protobyte.sdsserver.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.network.*
import io.ktor.websocket.*
import kotlin.collections.*

data class ClientInfo(val _name: String, val _location: String) {
    val name: String = _name
    val location: String = _location
}

fun Application.configureRouting() {

    val clients: MutableMap<NetworkAddress, ClientInfo> = mutableMapOf()

    routing {
        authenticate("auth-signage-digest") {
            webSocket("/signage") { // websocketSession
                for (frame in incoming) {
                    val text = (frame as? Frame.Text)!!.readText()
                    when (text[0]) {
                        'a' -> {
                            val len1 = text[1].code
                            val len2 = text[2].code
                            val name = text.slice(2..2+len1)
                            val location = text.slice(2+len1..2+len1+len2)
                            clients[
                                    NetworkAddress(this.call.request.origin.remoteHost,this.call.request.origin.port)
                            ] = ClientInfo(name,location)
                        }
                        'c' -> {

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
