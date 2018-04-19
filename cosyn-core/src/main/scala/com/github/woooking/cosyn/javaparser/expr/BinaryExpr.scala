package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr.Operator
import com.github.javaparser.ast.expr.{BinaryExpr => JPBinaryExpr}

class BinaryExpr(override val delegate: JPBinaryExpr) extends Expression[JPBinaryExpr] {
    val left: Expression[_ <: Node] = delegate.getLeft
    val ope: Operator = delegate.getOperator
    val right: Expression[_ <: Node] = delegate.getRight
}

object BinaryExpr {
    def apply(delegate: JPBinaryExpr): BinaryExpr = new BinaryExpr(delegate)

    def unapply(arg: BinaryExpr): Option[(
        Expression[_ <: Node],
            Operator,
            Expression[_ <: Node]
        )] = Some((
        arg.left,
        arg.ope,
        arg.right
    ))
}


