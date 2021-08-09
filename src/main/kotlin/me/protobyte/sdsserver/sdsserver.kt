package me.protobyte.sdsserver

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.protobyte.sdsserver.plugins.*
import me.protobyte.sdsserver.config.*

class SDSServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Config.load()
            embeddedServer(Netty, port = 17420, host = "0.0.0.0", watchPaths = listOf("classes")) {
                configureSecurity()
                configureHTTP()
                configureSessions()
                configureRouting()
            }.start(wait = true)
        }
    }
}


