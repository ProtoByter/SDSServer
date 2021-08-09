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
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
open class BaseMessage(val success: Boolean)

@Serializable
data class ErrorMessage(val error: String) : BaseMessage(false)

@Serializable
class SuccessMessage(
    val result: String
) : BaseMessage(true)

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

@Serializable
data class ResolvedRules(val rules: List<Rule>, val resources: Map<String,ByteArray>)

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
    var reloadExpiry: LocalDateTime = LocalDateTime.now().plusMinutes(5)
    var needReload: Boolean = true
        get() {
            if (LocalDateTime.now().isAfter(reloadExpiry)) {
                field = false
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

    fun writeRule(rule: Rule, file: File) {
        var outRule = ""
        for (rulePart in rule) {
            outRule += when (rulePart.type) {
                RuleTypes.On -> "ON"
                RuleTypes.Between -> "BETWEEN"
                RuleTypes.Display -> "DISPLAY"
                RuleTypes.Every -> "EVERY"
            }

            var args = " "
            rulePart.args.forEach { args += it }

            outRule += args
            outRule += " "
        }
        outRule += ";\n"
        file.appendText(outRule)
    }

    fun writeResource(resource: Map.Entry<String,ByteArray>) {
        val file = File("config/${resource.key}")
        file.writeBytes(resource.value)
    }

    fun writeRules(newRules: ResolvedRules) {
        // Clear the rule file
        val ruleFile = File("config/rules.sdsr")
        ruleFile.delete()
        ruleFile.createNewFile()

        // Write every resource
        for (entry in newRules.resources) {
            writeResource(entry)
        }

        // Write all the rules
        for (entry in newRules.rules) {
            writeRule(entry, ruleFile)
        }
    }

    var loadedUsers: List<User> = listOf()
    var loadedConfig: configJson = configJson("","", "")
    var loadedRules: List<Rule> = listOf()
}
