package me.protobyte.sdsserver.rules

import me.protobyte.sdsserver.config.Rule
import me.protobyte.sdsserver.config.RulePart
import me.protobyte.sdsserver.config.RuleTypes
import org.antlr.v4.runtime.ParserRuleContext

class RuleListener: SDSRuleBaseListener() {

    fun getChildrenWithoutInst(ctx: ParserRuleContext): List<String> {
        var children = ctx.children.map { it.text }
        children = children.subList(1,children.size)
        return children
    }

    override fun enterBetween(ctx: SDSRuleParser.BetweenContext) {
        rule.last().add(RulePart(RuleTypes.Between,getChildrenWithoutInst(ctx)))
    }

    override fun enterDisplay(ctx: SDSRuleParser.DisplayContext) {
        rule.last().add(RulePart(RuleTypes.Display,getChildrenWithoutInst(ctx)))
    }

    override fun enterEvery(ctx: SDSRuleParser.EveryContext) {
        rule.last().add(RulePart(RuleTypes.Every,getChildrenWithoutInst(ctx)))
    }

    override fun enterOn(ctx: SDSRuleParser.OnContext) {
        rule.last().add(RulePart(RuleTypes.On,getChildrenWithoutInst(ctx)))
    }

    override fun enterAt(ctx: SDSRuleParser.AtContext) {
        rule.last().add(RulePart(RuleTypes.At,getChildrenWithoutInst(ctx)))
    }

    override fun enterSds_statement(ctx: SDSRuleParser.Sds_statementContext) {
        rule.add(mutableListOf())
    }



    val rule: MutableList<Rule> = mutableListOf()

}