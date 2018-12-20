package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.expr.UnaryExpr

object UnaryOperator extends Enumeration {
    type UnaryOperator = Value

    val ClassOf: UnaryOperator = Value("classOf")
    val Plus: UnaryOperator = Value("+")
    val Minus: UnaryOperator = Value("-")
    val PrefixIncrement: UnaryOperator = Value("++p")
    val PrefixDecrement: UnaryOperator = Value("--p")
    val PostfixIncrement: UnaryOperator = Value("p++")
    val PostfixDecrement: UnaryOperator = Value("p--")
    val LogicalComplement: UnaryOperator = Value("!")
    val BitwiseComplement: UnaryOperator = Value("~")

    def fromAssignExprOperator(ope: UnaryExpr.Operator): UnaryOperator = ope match {
        case UnaryExpr.Operator.PLUS => Plus
        case UnaryExpr.Operator.MINUS => Minus
        case UnaryExpr.Operator.PREFIX_INCREMENT => PrefixIncrement
        case UnaryExpr.Operator.PREFIX_DECREMENT => PrefixDecrement
        case UnaryExpr.Operator.POSTFIX_INCREMENT => PostfixIncrement
        case UnaryExpr.Operator.POSTFIX_DECREMENT => PostfixDecrement
        case UnaryExpr.Operator.LOGICAL_COMPLEMENT => LogicalComplement
        case UnaryExpr.Operator.BITWISE_COMPLEMENT => BitwiseComplement
    }
}
