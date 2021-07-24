package me.protobyte

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.protobyte.plugins.*

fun main() {
    embeddedServer(Netty, port = 17420, host = "0.0.0.0") {
        configureRouting()
        configureSecurity()
        configureSockets()
        configureHTTP()
        configureAdministration()
    }.start(wait = true)

}
