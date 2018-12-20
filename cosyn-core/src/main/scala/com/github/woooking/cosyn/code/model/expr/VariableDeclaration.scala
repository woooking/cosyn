package com.github.woooking.cosyn.code.model.expr

import com.github.woooking.cosyn.code.model.Node
import com.github.woooking.cosyn.code.model.ty.{BasicType, Type}
import com.github.woooking.cosyn.util.CodeUtil

case class VariableDeclaration(ty: Type, name: String, init: Option[Expression]) extends Expression {
    init.foreach(_.parent = this)

    override def toString: String = init match {
        case None => s"${CodeUtil.qualifiedClassName2Simple(ty.toString)} $name"
        case Some(i) => s"${CodeUtil.qualifiedClassName2Simple(ty.toString)} $name = $i"
    }

    override def children: Seq[Node] = init.toSeq
}

object VariableDeclaration {
    def apply(ty: String, name: String): VariableDeclaration = VariableDeclaration(BasicType(ty), name, None)

    def apply(ty: String, name: String, init: Expression): VariableDeclaration = VariableDeclaration(BasicType(ty), name, Some(init))

    def apply(ty: Type, name: String): VariableDeclaration = VariableDeclaration(ty, name, None)

    def apply(ty: Type, name: String, init: Expression): VariableDeclaration = new VariableDeclaration(ty, name, Some(init))
}

