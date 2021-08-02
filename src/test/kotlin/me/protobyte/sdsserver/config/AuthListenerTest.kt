package me.protobyte.sdsserver.config

import me.protobyte.sdsserver.plugins.User
import me.protobyte.sdsserver.plugins.userTypes
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class AuthListenerTest {

    @Test
    fun getUsers() {
        val lexer = SDSAuthLexer(CharStreams.fromString("{T:O,P:\"aaaa\",U:\"bbbb\",R:\"cccc\"},{T:D,P:\"aaaa\",U:\"bbbb\",R:\"cccc\"},{T:D,P:\"aaaa\",U:\"bbbb\",R:\"cccc\"}"))
        val tokens = CommonTokenStream(lexer)
        val parser = SDSAuthParser(tokens)
        parser.buildParseTree = true
        val entryPoint = parser.entries()
        val walker = ParseTreeWalker()
        val listener = AuthListener()
        walker.walk(listener, entryPoint)
        val expected = listOf(User(userTypes.OAuth,"cccc","aaaa","bbbb"),User(userTypes.Digest,"cccc","aaaa","bbbb"),User(userTypes.Digest,"cccc","aaaa","bbbb"))
        assertEquals(expected,listener.users)
    }
}