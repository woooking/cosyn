package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{CharLiteralExpr => JPCharLiteralExpr}

class CharLiteralExpr(override val delegate: JPCharLiteralExpr) extends Expression[JPCharLiteralExpr] {
    val value: Char = delegate.asChar()
}

object CharLiteralExpr {
    def apply(delegate: JPCharLiteralExpr): CharLiteralExpr = new CharLiteralExpr(delegate)

    def unapply(arg: CharLiteralExpr): Option[Char] = Some(arg.value)
}
