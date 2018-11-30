package com.github.woooking.cosyn.pattern.model.expr

import com.github.woooking.cosyn.util.CodeUtil

case class EnumConstantExpr(enumType: String, name: NameOrHole) extends Expression {
    override def toString: String = s"${CodeUtil.qualifiedClassName2Simple(enumType)}.$name"
}

object EnumConstantExpr {
    def apply(enumType: String, name: NameOrHole): EnumConstantExpr = {
        val enumConstantExpr = new EnumConstantExpr(enumType, name)
        name.parent = enumConstantExpr
        enumConstantExpr
    }
}


