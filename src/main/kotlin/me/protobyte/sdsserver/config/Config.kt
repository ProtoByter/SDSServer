package me.protobyte.sdsserver.config

import com.beust.klaxon.Klaxon
import io.ktor.util.network.*
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.FileReader
import java.time.LocalDateTime
import me.protobyte.sdsserver.rules.parse as parse_rule
import kotlinx.serialization.*

enum class userTypes {
    OAuth,
    Digest
}

data class User(
    var type: userTypes,
    var realm: String,
    var password: String,
    var username: String
)

@Serializable
enum class RuleTypes {
    Between,
    Every,
    On,
    Display
}

@Serializable
data class RulePart(val type: RuleTypes, val args: List<String>)

typealias Rule = MutableList<RulePart>

fun getTextfromString(text: String): String {
    return text.trim { it <= '"'}
}



fun parse(entries: CharStream): List<User> {
    val lexer = SDSAuthLexer(entries)
    val tokens = CommonTokenStream(lexer)
    val parser = SDSAuthParser(tokens)
    val entryPoint = parser.entries()
    val walker = ParseTreeWalker()
    val listener = AuthListener()
    walker.walk(listener,entryPoint)

    return listener.users
}

class AuthListener : SDSAuthBaseListener() {

    override fun enterString(ctx: SDSAuthParser.StringContext) {
        if (ctx.parent is SDSAuthParser.Pw_entryContext) {
            users.last().password = getTextfromString(ctx.text)
        }
        else if (ctx.parent is SDSAuthParser.Realm_entryContext) {
            users.last().realm = getTextfromString(ctx.text)
        }
        else if (ctx.parent is SDSAuthParser.User_entryContext) {
            users.last().username = getTextfromString(ctx.text)
        }
    }

    override fun enterDigest(ctx: SDSAuthParser.DigestContext) {
        users.last().type = userTypes.Digest
    }

    override fun enterOauth(ctx: SDSAuthParser.OauthContext) {
        users.last().type = userTypes.OAuth
    }

    override fun enterEntry_end(ctx: SDSAuthParser.Entry_endContext?) {
        users.add(User(userTypes.Digest,"","",""))
    }

    override fun enterEntry_non_end(ctx: SDSAuthParser.Entry_non_endContext?) {
        users.add(User(userTypes.Digest,"","",""))
    }

    val users: MutableList<User> = mutableListOf()
}

data class OAuthEntry(var id: String, var expiry: LocalDateTime = LocalDateTime.now().plusMinutes(5))

object RuntimeState {
    var oauth2IPs: MutableMap<NetworkAddress,OAuthEntry> = mutableMapOf()
        get() {
            for (entry in field) {
                if (entry.value.expiry.isBefore(LocalDateTime.now())) {
                    field.remove(entry.key)
                }
            }
            return field
        }
}

data class configJson(val clientID: String, val clientSecret: String, val applicationID: String)

object Config {
    fun load() {
        loadedUsers = parse(CharStreams.fromFileName("config/users.sdsu"))
        loadedRules = parse_rule(CharStreams.fromFileName("config/rules.sdsr"))
        loadedConfig = Klaxon().parse<configJson>(FileReader("config/config.json").readText())!!
    }

    fun reload() {
        load()
    }
    var loadedUsers: List<User> = listOf()
    var loadedConfig: configJson = configJson("","", "")
    var loadedRules: List<Rule> = listOf()
}
