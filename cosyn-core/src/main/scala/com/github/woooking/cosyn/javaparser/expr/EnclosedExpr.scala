package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.{EnclosedExpr => JPEnclosedExpr}

class EnclosedExpr(override val delegate: JPEnclosedExpr) extends Expression[JPEnclosedExpr] {
    val inner: Expression[_ <: Node] = delegate.getInner
}

object EnclosedExpr {
    def apply(delegate: JPEnclosedExpr): EnclosedExpr = new EnclosedExpr(delegate)

    def unapply(arg: EnclosedExpr): Option[Expression[_ <: Node]] = Some(arg.inner)
}