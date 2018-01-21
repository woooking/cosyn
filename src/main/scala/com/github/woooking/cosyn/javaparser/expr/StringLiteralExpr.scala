package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{StringLiteralExpr => JPStringLiteralExpr}

class StringLiteralExpr(override val delegate: JPStringLiteralExpr) extends Expression[JPStringLiteralExpr] {
    val value: String = delegate.asString()
}

object StringLiteralExpr {
    def apply(delegate: JPStringLiteralExpr): StringLiteralExpr = new StringLiteralExpr(delegate)

    def unapply(arg: StringLiteralExpr): Option[String] = Some(arg.value)
}
