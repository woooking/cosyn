package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{UnaryExpr => JPUnaryExpr}

class UnaryExpr(override val delegate: JPUnaryExpr) extends Expression[JPUnaryExpr] {
    val ope: JPUnaryExpr.Operator = delegate.getOperator

    val expression: Expression[_] = delegate.getExpression
}

object UnaryExpr {
    def apply(delegate: JPUnaryExpr): UnaryExpr = new UnaryExpr(delegate)

    def unapply(arg: UnaryExpr): Option[(JPUnaryExpr.Operator, Expression[_])] = Some((arg.ope, arg.expression))
}