package me.protobyte.plugins

import com.google.gson.Gson
import io.ktor.auth.*
import io.ktor.util.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8
import java.io.File

fun getSHA256Digest(str: String): ByteArray = MessageDigest.getInstance("SHA256").digest(str.toByteArray(UTF_8))

class UserTable {
    data class UserTable (
        val manageUsers: List<UserEntry>,
        val signageUsers: List<UserEntry>
    )

    data class UserEntry (
        val username: String,
        val password: String,
    )
}

fun Application.configureSecurity() {
    install(Authentication) {
        var gson = Gson()
        val users = gson?.fromJson(File("users.json").readText(), UserTable.UserTable::class.java);
        val signageRealm = "Access to the '/signage' path"
        val manageRealm = "Access to the '/manage' path"
        val managementUsers = users.manageUsers.map{it.username to getSHA256Digest("$it.username:$manageRealm:$it.password")}.toMap();
        val signageUsers = users.signageUsers.map{it.username to getSHA256Digest("$it.username:$signageRealm:$it.password")}.toMap();
        digest("auth-signage-digest") {
            realm = signageRealm
            digestProvider { userName, realm ->
                signageUsers[userName]
            }
        }

        digest("auth-manage-digest") {
            realm = manageRealm
            digestProvider { userName, realm ->
                managementUsers[userName]
            }
        }
    }
}
