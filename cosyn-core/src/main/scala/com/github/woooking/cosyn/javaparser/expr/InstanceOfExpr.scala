package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.ReferenceType
import com.github.javaparser.ast.expr.{InstanceOfExpr => JPInstanceOfExpr}

class InstanceOfExpr(override val delegate: JPInstanceOfExpr) extends Expression[JPInstanceOfExpr] {
    val expression: Expression[_ <: Node] = delegate.getExpression
    val ty: ReferenceType = delegate.getType
}

object InstanceOfExpr {
    def apply(delegate: JPInstanceOfExpr): InstanceOfExpr = new InstanceOfExpr(delegate)

    def unapply(arg: InstanceOfExpr): Option[(
        Expression[_ <: Node],
            ReferenceType
        )] = Some((
        arg.expression,
        arg.ty
    ))
}


