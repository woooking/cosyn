package com.github.woooking.cosyn.pattern.model.expr

import com.github.woooking.cosyn.pattern.model.Node
import com.github.woooking.cosyn.pattern.model.ty.BasicType
import com.github.woooking.cosyn.util.CodeUtil.qualifiedClassName2Simple

case class EnumConstantExpr(enumType: BasicType, name: NameOrHole) extends Expression {
    name.parent = this
    override def toString: String = s"${qualifiedClassName2Simple(enumType.ty)}.$name"

    override def children: Seq[Node] = Seq(name)
}

object EnumConstantExpr {
    def apply(enumType: BasicType, name: NameOrHole): EnumConstantExpr = {
        val enumConstantExpr = new EnumConstantExpr(enumType, name)
        name.parent = enumConstantExpr
        enumConstantExpr
    }

    def apply(enumType: String, name: NameOrHole): EnumConstantExpr = {
        val enumConstantExpr = new EnumConstantExpr(BasicType(enumType), name)
        name.parent = enumConstantExpr
        enumConstantExpr
    }
}


