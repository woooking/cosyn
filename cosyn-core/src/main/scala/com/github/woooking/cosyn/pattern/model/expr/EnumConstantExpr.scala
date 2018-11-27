package com.github.woooking.cosyn.pattern.model.expr

case class EnumConstantExpr(enumType: String, name: NameOrHole) extends Expression

object EnumConstantExpr {
    def apply(enumType: String, name: NameOrHole): EnumConstantExpr = {
        val enumConstantExpr = new EnumConstantExpr(enumType, name)
        name.parent = enumConstantExpr
        enumConstantExpr
    }
}


