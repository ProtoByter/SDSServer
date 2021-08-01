package me.protobyte.sdsserver.rules

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

fun parse(rule: String): Rule {
    val lexer = SDSRuleLexer(CharStreams.fromString(rule))
    val tokens = CommonTokenStream(lexer)
    val parser = SDSRuleParser(tokens)
    parser.buildParseTree = true
    val entryPoint = parser.sds_statement()
    val walker = ParseTreeWalker()
    val listener = RuleListener()
    walker.walk(listener,entryPoint)

    return listener.rule
}

enum class RuleTypes {
    Between,
    Every,
    On,
    Display
}

data class RulePart(val _type: RuleTypes, val _args: List<String>) {
    val type = _type
    val args = _args
}

typealias Rule = MutableList<RulePart>

typealias RuleSet = MutableList<Rule>