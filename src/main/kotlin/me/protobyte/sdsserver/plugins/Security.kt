package me.protobyte.sdsserver.plugins

import io.ktor.application.*
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

fun getSHA256Digest(str: String): ByteArray = MessageDigest.getInstance("SHA256").digest(str.toByteArray(UTF_8))

data class User(
    var realm: String,
    var password: String,
    var username: String
)

class UserTable {
    data class UserTable(
        val manageUsers: MutableList<UserEntry>,
        val signageUsers: MutableList<UserEntry>
    )

    data class UserEntry(
        val username: String,
        val password: String,
    )
}

fun checkUser(digest: String, realm: String) {

}

fun Application.configureSecurity() {
    /*
    install(Authentication) {
        val signageRealm = "Access to the '/signage' path"
        val manageRealm = "Access to the '/manage' path"

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
    */
}
