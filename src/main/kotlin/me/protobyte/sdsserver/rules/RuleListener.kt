package me.protobyte.sdsserver.rules

import org.antlr.v4.runtime.ParserRuleContext

class RuleListener: SDSRuleBaseListener() {

    fun getChildrenWithoutInst(ctx: ParserRuleContext): List<String> {
        var children = ctx.children.map { it.text }
        children = children.subList(1,children.size)
        return children
    }

    override fun enterBetween(ctx: SDSRuleParser.BetweenContext) {
        rule.add(RulePart(RuleTypes.Between,getChildrenWithoutInst(ctx)))
    }

    override fun enterDisplay(ctx: SDSRuleParser.DisplayContext) {
        rule.add(RulePart(RuleTypes.Display,getChildrenWithoutInst(ctx)))
    }

    override fun enterEvery(ctx: SDSRuleParser.EveryContext) {
        rule.add(RulePart(RuleTypes.Every,getChildrenWithoutInst(ctx)))
    }

    override fun enterOn(ctx: SDSRuleParser.OnContext) {
        rule.add(RulePart(RuleTypes.On,getChildrenWithoutInst(ctx)))
    }

    val rule: Rule = mutableListOf()
}