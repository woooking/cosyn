package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{BooleanLiteralExpr => JPBooleanLiteralExpr}

class BooleanLiteralExpr(override val delegate: JPBooleanLiteralExpr) extends Expression[JPBooleanLiteralExpr] {
    val value: Boolean = delegate.getValue
}

object BooleanLiteralExpr {
    def apply(delegate: JPBooleanLiteralExpr): BooleanLiteralExpr = new BooleanLiteralExpr(delegate)

    def unapply(arg: BooleanLiteralExpr): Option[Boolean] = Some(arg.value)
}
