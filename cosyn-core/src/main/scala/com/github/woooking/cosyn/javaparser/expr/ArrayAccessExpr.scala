package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.{ArrayAccessExpr => JPArrayAccessExpr}

class ArrayAccessExpr(override val delegate: JPArrayAccessExpr) extends Expression[JPArrayAccessExpr] {
    val name: Expression[_ <: Node] = delegate.getName
    val index: Expression[_ <: Node] = delegate.getIndex
}

object ArrayAccessExpr {
    def apply(delegate: JPArrayAccessExpr): ArrayAccessExpr = new ArrayAccessExpr(delegate)

    def unapply(arg: ArrayAccessExpr): Option[(
        Expression[_ <: Node],
            Expression[_ <: Node]
        )] = Some((
        arg.name,
        arg.index
    ))
}


