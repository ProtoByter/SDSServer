package me.protobyte.sdsserver.config

import me.protobyte.sdsserver.plugins.User
import me.protobyte.sdsserver.plugins.userTypes
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

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

    override fun enterEnd_entry(ctx: SDSAuthParser.End_entryContext?) {
        users.add(User(userTypes.Digest,"","",""))
    }

    override fun enterEntry_non_end(ctx: SDSAuthParser.Entry_non_endContext?) {
        users.add(User(userTypes.Digest,"","",""))
    }

    val users: MutableList<User> = mutableListOf()
}