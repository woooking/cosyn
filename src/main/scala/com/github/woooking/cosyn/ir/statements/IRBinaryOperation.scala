package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.statements.BinaryOperator.BinaryOperator
import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRBinaryOperation(target: IRVariable, ope: BinaryOperator, lhs: IRExpression, rhs: IRExpression) extends IRAbstractStatement {

}
