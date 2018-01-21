package com.github.woooking.cosyn.ir.statements

import com.github.javaparser.ast.expr.BinaryExpr
import com.github.woooking.cosyn.javaparser.expr.AssignExpr

object BinaryOperator extends Enumeration {
    type BinaryOperator = Value

    val Plus: BinaryOperator = Value("+")
    val Minus: BinaryOperator = Value("-")
    val Multiply: BinaryOperator = Value("*")
    val Divide: BinaryOperator = Value("/")
    val BitAnd: BinaryOperator = Value("&")
    val BitOr: BinaryOperator = Value("|")
    val BitXor: BinaryOperator = Value("^")
    val And: BinaryOperator = Value("&&")
    val Or: BinaryOperator = Value("||")
    val Remainder: BinaryOperator = Value("%")
    val LeftShift: BinaryOperator = Value("<<")
    val SignedRightShift: BinaryOperator = Value(">>")
    val UnsignedRightShift: BinaryOperator = Value(">>>")
    val Equals: BinaryOperator = Value("==")
    val NotEquals: BinaryOperator = Value("!=")
    val Less: BinaryOperator = Value("<")
    val Greater: BinaryOperator = Value(">")
    val LessEqual: BinaryOperator = Value("<=")
    val GreaterEqual: BinaryOperator = Value(">=")

    def fromAssignExprOperator(ope: AssignExpr.Operator.Operator): BinaryOperator = ope match {
        case AssignExpr.Operator.Plus => Plus
        case AssignExpr.Operator.Minus => Minus
        case AssignExpr.Operator.Multiply => Multiply
        case AssignExpr.Operator.Divide => Divide
        case AssignExpr.Operator.And => BitAnd
        case AssignExpr.Operator.Or => BitOr
        case AssignExpr.Operator.Xor => BitXor
        case AssignExpr.Operator.Remainder => Remainder
        case AssignExpr.Operator.LeftShift => LeftShift
        case AssignExpr.Operator.SignedRightShift => SignedRightShift
        case AssignExpr.Operator.UnsignedRightShift => UnsignedRightShift
        case AssignExpr.Operator.Assign => throw new Exception("Cannot convert assign operator to binary operator!")
    }

    def fromBinaryExprOperator(ope: BinaryExpr.Operator): BinaryOperator = ope match {
        case BinaryExpr.Operator.PLUS => Plus
        case BinaryExpr.Operator.MINUS => Minus
        case BinaryExpr.Operator.MULTIPLY => Multiply
        case BinaryExpr.Operator.DIVIDE => Divide
        case BinaryExpr.Operator.AND => And
        case BinaryExpr.Operator.OR => Or
        case BinaryExpr.Operator.BINARY_AND => BitAnd
        case BinaryExpr.Operator.BINARY_OR => BitOr
        case BinaryExpr.Operator.XOR => BitXor
        case BinaryExpr.Operator.REMAINDER => Remainder
        case BinaryExpr.Operator.LEFT_SHIFT => LeftShift
        case BinaryExpr.Operator.SIGNED_RIGHT_SHIFT => SignedRightShift
        case BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT => UnsignedRightShift
        case BinaryExpr.Operator.EQUALS => Equals
        case BinaryExpr.Operator.NOT_EQUALS => NotEquals
        case BinaryExpr.Operator.LESS => Less
        case BinaryExpr.Operator.GREATER => Greater
        case BinaryExpr.Operator.LESS_EQUALS => LessEqual
        case BinaryExpr.Operator.GREATER_EQUALS => GreaterEqual
    }

}
