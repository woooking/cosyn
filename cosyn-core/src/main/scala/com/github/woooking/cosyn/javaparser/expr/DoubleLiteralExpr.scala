package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{DoubleLiteralExpr => JPDoubleLiteralExpr}

class DoubleLiteralExpr(override val delegate: JPDoubleLiteralExpr) extends Expression[JPDoubleLiteralExpr] {
    val value: Double = delegate.asDouble()
}

object DoubleLiteralExpr {
    def apply(delegate: JPDoubleLiteralExpr): DoubleLiteralExpr = new DoubleLiteralExpr(delegate)

    def unapply(arg: DoubleLiteralExpr): Option[Double] = Some(arg.value)
}