package com.github.woooking.cosyn.pattern.model.expr

import com.github.woooking.cosyn.pattern.model.Node
import com.github.woooking.cosyn.util.CodeUtil

case class VariableDeclaration(ty: String, name: String, init: Option[Expression]) extends Expression {
    init.foreach(_.parent = this)

    override def toString: String = init match {
        case None => s"${CodeUtil.qualifiedClassName2Simple(ty)} $name"
        case Some(i) => s"${CodeUtil.qualifiedClassName2Simple(ty)} $name = $i"
    }

    override def children: Seq[Node] = init.toSeq
}

object VariableDeclaration {
    def apply(ty: String, name: String): VariableDeclaration = new VariableDeclaration(ty, name, None)

    def apply(ty: String, name: String, init: Expression): VariableDeclaration = new VariableDeclaration(ty, name, Some(init))
}

