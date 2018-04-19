package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.{ConditionalExpr => JPConditionalExpr}

class ConditionalExpr(override val delegate: JPConditionalExpr) extends Expression[JPConditionalExpr] {
    val condition: Expression[_ <: Node] = delegate.getCondition
    val thenExpr: Expression[_ <: Node] = delegate.getThenExpr
    val elseExpr: Expression[_ <: Node] = delegate.getElseExpr
}

object ConditionalExpr {
    def apply(delegate: JPConditionalExpr): ConditionalExpr = new ConditionalExpr(delegate)

    def unapply(arg: ConditionalExpr): Option[(
        Expression[_ <: Node],
            Expression[_ <: Node],
            Expression[_ <: Node],
        )] = Some((
        arg.condition,
        arg.thenExpr,
        arg.elseExpr
    ))
}