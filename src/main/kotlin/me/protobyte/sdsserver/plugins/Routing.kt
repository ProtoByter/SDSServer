package me.protobyte.sdsserver.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.network.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.protobyte.sdsserver.config.*
import java.io.FileReader
import kotlin.collections.*

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
    val rules = Config.loadedRules
    val requireResolve: MutableList<Rule> = mutableListOf()
    for (rule in rules) {
	    requireResolve.add(rule.filter { it.type == RuleTypes.Display } as Rule)
    }
    requireResolve.forEach { it.forEach {
        resolvedResources[it.args[0]] = FileReader("config/${it.args[0]}").readText().toByteArray()
    } }
    return ResolvedRules(Config.loadedRules, resolvedResources)
}

fun Application.configureRouting() {

    val clients: MutableMap<NetworkAddress, ClientInfo> = mutableMapOf()

    routing {
        authenticate("auth-signage-digest") {
            post("/digest/add") {
                val name = call.request.queryParameters["name"]
                val location = call.request.queryParameters["location"]
                if (name == null || location == null) {
                    call.respond(HttpStatusCode.BadRequest,ErrorMessage("Missing parameters"))
                }
                else {
                    clients[
                            NetworkAddress(call.request.origin.remoteHost, call.request.origin.port)
                    ] = ClientInfo(name, location)
                    call.respond(HttpStatusCode.OK,SuccessMessage("n/a"))
                }
            }
            get("/digest/getConfig") {
                call.respondText(Json.encodeToString(SuccessMessage(result=resolveResources())))
            }
        }

        authenticate("auth-manage-ouath") {
            get("/secure/login") {

            }
            get("/secure/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/secure/getConfig")
            }
        }

        // Still secure since everything has a isAuthenticated call (which does OAuth2 authentication)

        post("/secure/setConfig") {
            if (isAuthenticated(call)) {
                call.respond(HttpStatusCode.NotImplemented,Json.encodeToString(ErrorMessage("This endpoint hasn't been implemented yet!")))
            }
            else {
                call.respond(HttpStatusCode.Forbidden,Json.encodeToString(ErrorMessage(error="Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")))
            }
        }

        get("/secure/getConfig") {
            if (isAuthenticated(call)) {
                call.respondText(Json.encodeToString(SuccessMessage(result=resolveResources())))
            }
            else {
                call.respond(HttpStatusCode.Forbidden,Json.encodeToString(ErrorMessage(error="Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")))
            }
        }

        get("/secure/clientList") {
            if (isAuthenticated(call)) {
                call.respondText(Json.encodeToString(SuccessMessage(result=clients)))
            }
            else {
                call.respond(HttpStatusCode.Forbidden,Json.encodeToString(ErrorMessage(error="Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")))
            }
        }

        post("/cecure/reload") {
            if (isAuthenticated(call)) {
                try {
                    Config.reload()
                }
                catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,Json.encodeToString(ErrorMessage(error="Internal Server Error. Couldn't reload configuration files, if you're the administrator then check that the configuration files are valid, and if you aren't the admin then please report this error to the admin")))
                }
                call.respond(HttpStatusCode.OK,Json.encodeToString(SuccessMessage("n/a")))
            }
            else {
                call.respond(HttpStatusCode.Forbidden,Json.encodeToString(ErrorMessage(error="Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")))
            }
        }
    }
}
