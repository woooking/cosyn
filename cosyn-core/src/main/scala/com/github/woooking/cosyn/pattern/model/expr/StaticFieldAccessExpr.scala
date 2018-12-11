package com.github.woooking.cosyn.pattern.model.expr

import com.github.woooking.cosyn.pattern.model.Node
import com.github.woooking.cosyn.util.CodeUtil.qualifiedClassName2Simple

case class StaticFieldAccessExpr(ty: String, name: NameOrHole) extends Expression {
    name.parent = this
    override def toString: String = s"${qualifiedClassName2Simple(ty)}.$name"

    override def children: Seq[Node] = Seq(name)
}




