package com.github.woooking.cosyn.pattern.model.expr

case class NameExpr(name: String) extends Expression with NameOrHole

object NameExpr {
    implicit def str2expr(name: String): NameExpr = NameExpr(name)
}
