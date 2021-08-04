package me.protobyte.sdsserver.rules

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import me.protobyte.sdsserver.config.*
import org.antlr.v4.runtime.CharStream

fun parse(rule: String): List<Rule> {
    return parse(CharStreams.fromString(rule))
}

fun parse(stream: CharStream): List<Rule> {
    val lexer = SDSRuleLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = SDSRuleParser(tokens)
    parser.buildParseTree = true
    val entryPoint = parser.sds_statement()
    val walker = ParseTreeWalker()
    val listener = RuleListener()
    walker.walk(listener,entryPoint)

    return listener.rule
}
