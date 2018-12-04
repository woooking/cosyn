package com.github.woooking.cosyn.pattern.model.expr

import com.github.woooking.cosyn.util.CodeUtil.qualifiedClassName2Simple

case class EnumConstantExpr(enumType: String, name: NameOrHole) extends Expression {
    name.parent = this
    override def toString: String = s"${qualifiedClassName2Simple(enumType)}.$name"
}

object EnumConstantExpr {
    def apply(enumType: String, name: NameOrHole): EnumConstantExpr = {
        val enumConstantExpr = new EnumConstantExpr(enumType, name)
        name.parent = enumConstantExpr
        enumConstantExpr
    }
}


