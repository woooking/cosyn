package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{ConditionalExpr => JPConditionalExpr}

class ConditionalExpr(override val delegate: JPConditionalExpr) extends Expression[JPConditionalExpr] {
    val condition: Expression[_] = delegate.getCondition
    val thenExpr: Expression[_] = delegate.getThenExpr
    val elseExpr: Expression[_] = delegate.getElseExpr
}

object ConditionalExpr {
    def apply(delegate: JPConditionalExpr): ConditionalExpr = new ConditionalExpr(delegate)

    def unapply(arg: ConditionalExpr): Option[(
        Expression[_],
            Expression[_],
            Expression[_],
        )] = Some((
        arg.condition,
        arg.thenExpr,
        arg.elseExpr
    ))
}