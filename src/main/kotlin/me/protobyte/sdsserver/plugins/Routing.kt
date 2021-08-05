package me.protobyte.sdsserver.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.sessions.*
import io.ktor.util.network.*
import io.ktor.websocket.*
import kotlin.collections.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import me.protobyte.sdsserver.config.*
import java.io.FileReader

@Serializable
data class ClientInfo(val name: String, val location: String)

suspend fun isAuthenticated(call: ApplicationCall): Boolean {
    val userSession: UserSession? = call.sessions.get<UserSession>()
    return if (userSession != null) {
        checkUserOAuth(userSession.token, NetworkAddress(call.request.origin.remoteHost,call.request.origin.port))
    }
    else {
        false
    }
}

suspend fun resolveResources(): ResolvedRules {
    val resolvedResources: MutableMap<String,ByteArray> = mutableMapOf()
    val requireResolve = Config.loadedRules
    requireResolve.forEach { it.filter { it.type == RuleTypes.On } }
    requireResolve.forEach { it.forEach {
        resolvedResources[it.args[0]] = FileReader("config/${it.args[0]}").readText().toByteArray()
    } }
    return ResolvedRules(Config.loadedRules, resolvedResources)
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

        authenticate("auth-manage-ouath") {
            get("/login") {

            }
            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/getConfig")
            }
        }

        post("/setConfig") {
            if (isAuthenticated(call)) {
                call.respondText("Not implemented yet")
            }
            else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        get("/getConfigO") {
            if (isAuthenticated(call)) {
                call.respondText(Json.encodeToString(resolveResources()))
            }
            else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        get("/clientList") {
            if (isAuthenticated(call)) {
                call.respondText(Json.encodeToString(clients))
            }
            else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        post("/reload") {
            if (isAuthenticated(call)) {
                Config.reload()
            }
            else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}
