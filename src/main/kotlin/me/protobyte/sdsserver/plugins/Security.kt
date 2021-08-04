package me.protobyte.sdsserver.plugins

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.network.*
import me.protobyte.sdsserver.config.parse
import org.antlr.v4.runtime.CharStreams
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8
import me.protobyte.sdsserver.config.*

fun getSHA256Digest(str: String): ByteArray = MessageDigest.getInstance("SHA256").digest(str.toByteArray(UTF_8))

suspend fun checkUserOAuth(token: String, ip: NetworkAddress): Boolean {
    if (ip !in RuntimeState.oauth2IPs) {
        val client = HttpClient() {
            expectSuccess = false
        }
        val meAPICall: HttpResponse = client.get("https://graph.microsoft.com/v1.0/me") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (meAPICall.status != HttpStatusCode.OK) {
            return false
        }

        val id = (Parser.Companion.default().parse(meAPICall.readText()) as JsonObject)["id"] as String
        RuntimeState.oauth2IPs[ip] = OAuthEntry(id)
    }

    val users = Config.loadedUsers.filter { it.type == userTypes.OAuth }.map { it.username to it }.toMap()

    return (RuntimeState.oauth2IPs[ip]!!.id in users)

}

fun getUserDigest(username: String, realm: String): ByteArray? {
    val users = parse(CharStreams.fromFileName("dist/users.sdsu")).toMutableList()
    for (user in users) {
        if (user.type == userTypes.OAuth) {
	        users.remove(user)
        }
        else if (user.realm != realm) {
            users.remove(user)
        }
    }

    val user = users.map{ it.username to it }.toMap()[username]

    return if (user == null) null else getSHA256Digest("${user.username}:${user.realm}:${user.password}")

}

fun Application.configureSecurity() {
    val httpClient = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    install (Authentication) {
        val signageRealm = "Access to the '/signage' path"

        digest("auth-signage-digest") {
            realm = signageRealm
            digestProvider { userName, realm ->
                getUserDigest(userName,realm)
            }
        }

        oauth("auth-manage-ouath") {
            urlProvider = { "http://localhost:17420/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "microsoft",
                    authorizeUrl = "https://login.microsoftonline.com/${Config.loadedConfig.applicationID}/oauth2/v2.0/authorize",
                    accessTokenUrl = "https://login.microsoftonline.com/${Config.loadedConfig.applicationID}/oauth2/v2.0/token",
                    requestMethod = HttpMethod.Post,
                    clientId = Config.loadedConfig.clientID,
                    clientSecret = Config.loadedConfig.clientSecret,
                    defaultScopes = listOf("https://graph.microsoft.com/User.Read")
                )
            }
            client = httpClient
        }
    }

}
