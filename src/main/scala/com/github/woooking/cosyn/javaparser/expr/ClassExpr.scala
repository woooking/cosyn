package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{ClassExpr => JPClassExpr}

class ClassExpr(override val delegate: JPClassExpr) extends Expression[JPClassExpr] {
    val ty: Type = delegate.getType
}

object ClassExpr {
    def apply(delegate: JPClassExpr): ClassExpr = new ClassExpr(delegate)

    def unapply(arg: ClassExpr): Option[Type] = Some(arg.ty)
}