package me.protobyte.sdsserver.rules

import me.protobyte.sdsserver.config.RulePart
import me.protobyte.sdsserver.config.RuleTypes
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class RulesKtTest {

    @Test
    fun ParseTest1() {
        val test = parse("BETWEEN 08:00 16:00 DISPLAY image0.png ON screen0 EVERY 5m;")[0]

        assertEquals(mutableListOf(
            RulePart(
                RuleTypes.Between,
                listOf("08:00","16:00")
            ),
            RulePart(
                RuleTypes.Display,
                listOf("image0.png")
            ),
            RulePart(
                RuleTypes.On,
                listOf("screen0")
            ),
            RulePart(
                RuleTypes.Every,
                listOf("5m")
            )
        ),test)
    }

    @Test
    fun ParseTest2() {
        val test = parse("DISPLAY image0.png ON screen0;")[0]

        assertEquals(mutableListOf(
            RulePart(
                RuleTypes.Display,
                listOf("image0.png")
            ),
            RulePart(
                RuleTypes.On,
                listOf("screen0")
            )
        ),test)
    }

    @Test
    fun ParseTest3() {
        val test = parse("EVERY 5m 00:00 DISPLAY image0.png;")[0]

        assertEquals(mutableListOf(
            RulePart(
                RuleTypes.Every,
                listOf("5m","00:00")
            ),
            RulePart(
                RuleTypes.Display,
                listOf("image0.png")
            )
        ),test)
    }
}