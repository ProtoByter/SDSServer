package me.protobyte.sdsserver

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.protobyte.sdsserver.plugins.*

class SDSServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            embeddedServer(Netty, port = 17420, host = "0.0.0.0") {
                configureRouting()
                configureSecurity()
                configureSockets()
                configureHTTP()
                configureAdministration()
            }.start(wait = true)
        }
    }
}


