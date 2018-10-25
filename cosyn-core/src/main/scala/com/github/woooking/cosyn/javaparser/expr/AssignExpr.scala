package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.{AssignExpr => JPAssignExpr}

class AssignExpr(override val delegate: JPAssignExpr) extends Expression[JPAssignExpr] {
    val target: Expression[_ <: Node] = Expression(delegate.getTarget)
    val ope: AssignExpr.Operator.Operator = AssignExpr.Operator(delegate.getOperator)
    val value: Expression[_ <: Node] = Expression(delegate.getValue)
}

object AssignExpr {
    def apply(delegate: JPAssignExpr): AssignExpr = new AssignExpr(delegate)

    def unapply(arg: AssignExpr): Option[(
        Expression[_ <: Node],
            AssignExpr.Operator.Operator,
            Expression[_ <: Node]
        )] = Some((
        arg.target,
        arg.ope,
        arg.value
    ))

    object Operator extends Enumeration {
        type Operator = Value

        val Assign: Operator = Value("=")
        val Plus: Operator = Value("+=")
        val Minus: Operator = Value("-=")
        val Multiply: Operator = Value("*=")
        val Divide: Operator = Value("/=")
        val And: Operator = Value("&=")
        val Or: Operator = Value("|=")
        val Xor: Operator = Value("^=")
        val Remainder: Operator = Value("%=")
        val LeftShift: Operator = Value("<<=")
        val SignedRightShift: Operator = Value(">>=")
        val UnsignedRightShift: Operator = Value(">>>=")

        def apply(n: JPAssignExpr.Operator): Operator = n match {
            case JPAssignExpr.Operator.ASSIGN => AssignExpr.Operator.Assign
            case JPAssignExpr.Operator.PLUS => AssignExpr.Operator.Plus
            case JPAssignExpr.Operator.MINUS => AssignExpr.Operator.Minus
            case JPAssignExpr.Operator.MULTIPLY => AssignExpr.Operator.Multiply
            case JPAssignExpr.Operator.DIVIDE => AssignExpr.Operator.Divide
            case JPAssignExpr.Operator.BINARY_AND => AssignExpr.Operator.And
            case JPAssignExpr.Operator.BINARY_OR => AssignExpr.Operator.Or
            case JPAssignExpr.Operator.XOR => AssignExpr.Operator.Xor
            case JPAssignExpr.Operator.REMAINDER => AssignExpr.Operator.Remainder
            case JPAssignExpr.Operator.LEFT_SHIFT => AssignExpr.Operator.LeftShift
            case JPAssignExpr.Operator.SIGNED_RIGHT_SHIFT => AssignExpr.Operator.SignedRightShift
            case JPAssignExpr.Operator.UNSIGNED_RIGHT_SHIFT => AssignExpr.Operator.UnsignedRightShift
        }
    }

}
