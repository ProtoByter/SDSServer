package me.protobyte.sdsserver.config

import me.protobyte.sdsserver.plugins.User

fun getTextfromString(text: String): String {
    return text.trim { it <= '"'}
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

    override fun enterEnd_entry(ctx: SDSAuthParser.End_entryContext?) {
        users.add(User("","",""))
    }

    override fun enterEntry_non_end(ctx: SDSAuthParser.Entry_non_endContext?) {
        users.add(User("","",""))
    }

    val users: MutableList<User> = mutableListOf()
}