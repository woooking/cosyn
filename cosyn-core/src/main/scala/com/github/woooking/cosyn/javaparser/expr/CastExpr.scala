package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{CastExpr => JPCastExpr}

class CastExpr(override val delegate: JPCastExpr) extends Expression[JPCastExpr] {
    val ty: Type = delegate.getType
    val expression: Expression[_ <: Node] = delegate.getExpression
}

object CastExpr {
    def apply(delegate: JPCastExpr): CastExpr = new CastExpr(delegate)

    def unapply(arg: CastExpr): Option[(Type, Expression[_ <: Node])] = Some((arg.ty, arg.expression))
}