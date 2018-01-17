package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{NameExpr => JPNameExpr}

class NameExpr(override val delegate: JPNameExpr) extends Expression[JPNameExpr] {
    val name: String = delegate.getName
}

object NameExpr {
    def apply(delegate: JPNameExpr): NameExpr = new NameExpr(delegate)

    def unapply(arg: NameExpr): Option[String] = Some(arg.name)
}
