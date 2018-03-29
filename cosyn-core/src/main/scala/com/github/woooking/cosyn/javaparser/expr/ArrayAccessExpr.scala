package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{ArrayAccessExpr => JPArrayAccessExpr}

class ArrayAccessExpr(override val delegate: JPArrayAccessExpr) extends Expression[JPArrayAccessExpr] {
    val name: Expression[_] = delegate.getName
    val index: Expression[_] = delegate.getIndex
}

object ArrayAccessExpr {
    def apply(delegate: JPArrayAccessExpr): ArrayAccessExpr = new ArrayAccessExpr(delegate)

    def unapply(arg: ArrayAccessExpr): Option[(
        Expression[_],
            Expression[_]
        )] = Some((
        arg.name,
        arg.index
    ))
}


