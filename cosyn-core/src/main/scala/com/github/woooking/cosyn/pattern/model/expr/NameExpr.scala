package com.github.woooking.cosyn.pattern.model.expr
import com.github.woooking.cosyn.pattern.model.Node

case class NameExpr(name: String) extends Expression with NameOrHole {
    override def toString: String = name

    override def children: Seq[Node] = Seq()
}

object NameExpr {
    implicit def str2expr(name: String): NameExpr = NameExpr(name)
}
