package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{IntegerLiteralExpr => JPIntegerLiteralExpr}

class IntegerLiteralExpr(override val delegate: JPIntegerLiteralExpr) extends Expression[JPIntegerLiteralExpr] {
    val value: Int = delegate.asInt()
}

object IntegerLiteralExpr {
    def apply(delegate: JPIntegerLiteralExpr): IntegerLiteralExpr = new IntegerLiteralExpr(delegate)

    def unapply(arg: IntegerLiteralExpr): Option[Int] = Some(arg.value)
}
