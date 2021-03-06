package me.protobyte.sdsserver.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.util.network.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.protobyte.sdsserver.config.*
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import kotlin.collections.*
import java.util.Base64

suspend fun isAuthenticated(call: ApplicationCall): Boolean {
    val userSession: UserSession? = call.sessions.get<UserSession>()
    return if (userSession != null) {
        checkUserOAuth(userSession.token, NetworkAddress(call.request.origin.remoteHost,call.request.origin.port))
    }
    else {
        false
    }
}

fun resolveResources(): ResolvedRules {
    val resolvedResources: MutableMap<String,String> = mutableMapOf()
    val rules = Config.loadedRules
    val requireResolve: MutableList<Rule> = mutableListOf()
    for (rule in rules) {
	    requireResolve.add(rule.filter { it.type == RuleTypes.Display } as Rule)
    }
    requireResolve.forEach { it.forEach {
        resolvedResources[it.args[0]] = Base64.getEncoder().encodeToString(File("config/${it.args[0]}").readBytes())
    } }
    return ResolvedRules(Config.loadedRules, resolvedResources)
}

fun Application.configureRouting() {
    routing {
        authenticate("auth-signage-digest") {
            get("/digest/getConfig") {
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.OK
                ) { Json.encodeToString(SuccessRules(result = resolveResources())) }
            }

            get("/digest/needReload") {
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.OK
                ) { Json.encodeToString(needReloadMessage(result = RuntimeState.needReload)) }
            }
        }

        authenticate("auth-manage-ouath") {
            get("/secure/login") {

            }
            get("/secure/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/secure/")
            }
        }

        get("/secure/") {
            call.respondText("Successfully authenticated")
        }

        // Still secure since everything has a isAuthenticated call (which checks OAuth2 authentication)

        post("/secure/setConfig") {
            if (isAuthenticated(call)) {
                val newConfig = call.receiveText()
                if (newConfig == "") {
                    call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.BadRequest
                    ) { Json.encodeToString(ErrorMessage("You did not provide any data to update with!")) }
                }
                else {
                    try {
                        val newRules = Json.decodeFromString<ResolvedRules>(newConfig)
                        Config.writeRules(newRules)
                        RuntimeState.reloadExpiry = LocalDateTime.now().plusSeconds(2)
                        RuntimeState.needReload = true
                        call.respondText(
                            contentType = ContentType.Application.Json, HttpStatusCode.OK
                        ) { Json.encodeToString(SuccessText("Successfully updated")) }
                    }
                    catch (e: Exception) {
                        call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.BadRequest
                        ) { Json.encodeToString(ErrorMessage("You provided incorrect data to update with!")) }
                    }
                }
            }
            else {
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.Forbidden
                ) { Json.encodeToString(ErrorMessage(error = "Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")) }
            }
        }

        get("/secure/getConfig") {
            if (isAuthenticated(call)) {
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.OK
                ) { Json.encodeToString(SuccessRules(result = resolveResources())) }
            }
            else {
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.Forbidden
                ) { Json.encodeToString(ErrorMessage(error = "Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")) }
            }
        }

        post("/secure/reload") {
            if (isAuthenticated(call)) {
                try {
                    Config.reload()
                    RuntimeState.reloadExpiry = LocalDateTime.now().plusMinutes(5)
                    RuntimeState.needReload = true
                }
                catch (e: Exception) {
                    call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.InternalServerError
                    ) { Json.encodeToString(ErrorMessage(error = "Internal Server Error. Couldn't reload configuration files, if you're the administrator then check that the configuration files are valid, and if you aren't the admin then please report this error to the admin")) }
                }
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.OK
                ) { Json.encodeToString(SuccessText("n/a")) }
            }
            else {
                call.respondText(contentType = ContentType.Application.Json,HttpStatusCode.Forbidden
                ) { Json.encodeToString(ErrorMessage(error = "Not authenticated. This endpoint requires OAuth authentication with MS Azure AAD")) }
            }
        }
    }
}
