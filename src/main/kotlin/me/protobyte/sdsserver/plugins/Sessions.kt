package me.protobyte.sdsserver.plugins

import io.ktor.application.*
import io.ktor.sessions.*
import me.protobyte.sdsserver.config.*

data class UserSession(val token: String)

fun Application.configureSessions() {
    install(Sessions) {
        cookie<UserSession>("user_session")
    }
}