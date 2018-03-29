package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{EnclosedExpr => JPEnclosedExpr}

class EnclosedExpr(override val delegate: JPEnclosedExpr) extends Expression[JPEnclosedExpr] {
    val inner: Expression[_] = delegate.getInner
}

object EnclosedExpr {
    def apply(delegate: JPEnclosedExpr): EnclosedExpr = new EnclosedExpr(delegate)

    def unapply(arg: EnclosedExpr): Option[Expression[_]] = Some(arg.inner)
}