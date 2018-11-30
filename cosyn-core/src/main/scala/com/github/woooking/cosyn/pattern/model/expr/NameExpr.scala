package com.github.woooking.cosyn.pattern.model.expr

case class NameExpr(name: String) extends Expression with NameOrHole {
    override def toString: String = name
}

object NameExpr {
    implicit def str2expr(name: String): NameExpr = NameExpr(name)
}
