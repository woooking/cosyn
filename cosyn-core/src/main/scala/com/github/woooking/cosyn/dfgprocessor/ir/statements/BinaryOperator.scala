package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.javaparser.ast.expr.{AssignExpr, BinaryExpr}

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

    def fromAssignExprOperator(ope: AssignExpr.Operator): BinaryOperator = ope match {
        case AssignExpr.Operator.PLUS => Plus
        case AssignExpr.Operator.MINUS => Minus
        case AssignExpr.Operator.MULTIPLY => Multiply
        case AssignExpr.Operator.DIVIDE => Divide
        case AssignExpr.Operator.BINARY_AND => BitAnd
        case AssignExpr.Operator.BINARY_OR => BitOr
        case AssignExpr.Operator.XOR => BitXor
        case AssignExpr.Operator.REMAINDER => Remainder
        case AssignExpr.Operator.LEFT_SHIFT => LeftShift
        case AssignExpr.Operator.SIGNED_RIGHT_SHIFT => SignedRightShift
        case AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT => UnsignedRightShift
        case AssignExpr.Operator.ASSIGN => throw new Exception("Cannot convert assign operator to binary operator!")
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
