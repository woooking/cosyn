package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{LongLiteralExpr => JPLongLiteralExpr}

class LongLiteralExpr(override val delegate: JPLongLiteralExpr) extends Expression[JPLongLiteralExpr] {
    val value: Long = delegate.asLong()
}

object LongLiteralExpr {
    def apply(delegate: JPLongLiteralExpr): LongLiteralExpr = new LongLiteralExpr(delegate)

    def unapply(arg: LongLiteralExpr): Option[Long] = Some(arg.value)
}
