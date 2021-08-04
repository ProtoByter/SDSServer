package me.protobyte.sdsserver.rules

import me.protobyte.sdsserver.config.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class RuleListenerTest {

    @Test
    fun getRule() {
        val lexer = SDSRuleLexer(CharStreams.fromString("""
            BETWEEN 08:00 16:00 EVERY 5m 00:00 DISPLAY image0.png ON screen0;
            ON screen0 DISPLAY image0.png;
            EVERY 5m DISPLAY image0.png;
        """.trimIndent()))
        val tokens = CommonTokenStream(lexer)
        val parser = SDSRuleParser(tokens)
        parser.buildParseTree = true
        val entryPoint = parser.sds_statements()
        val walker = ParseTreeWalker()
        val listener = RuleListener()
        walker.walk(listener, entryPoint)
        val expected: List<Rule> = listOf(
            mutableListOf(
                RulePart(RuleTypes.Between,listOf("08:00","16:00")),
                RulePart(RuleTypes.Every,listOf("5m","00:00")),
                RulePart(RuleTypes.Display,listOf("image0.png")),
                RulePart(RuleTypes.On,listOf("screen0"))
            ),
            mutableListOf(
                RulePart(RuleTypes.On,listOf("screen0")),
                RulePart(RuleTypes.Display,listOf("image0.png"))
            ),
            mutableListOf(
                RulePart(RuleTypes.Every,listOf("5m")),
                RulePart(RuleTypes.Display,listOf("image0.png"))
            )
        )
        assertEquals(expected,listener.rule)
    }
}