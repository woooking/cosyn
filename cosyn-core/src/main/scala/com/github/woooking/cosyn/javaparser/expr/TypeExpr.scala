package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{TypeExpr => JPTypeExpr}
import com.github.javaparser.ast.`type`.Type

class TypeExpr(override val delegate: JPTypeExpr) extends Expression[JPTypeExpr] {
    val ty: Type = delegate.getType
}

object TypeExpr {
    def apply(delegate: JPTypeExpr): TypeExpr = new TypeExpr(delegate)

    def unapply(arg: TypeExpr): Option[Type] = Some(arg.ty)
}