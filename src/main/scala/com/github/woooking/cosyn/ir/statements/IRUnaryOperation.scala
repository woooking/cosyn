package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.statements.UnaryOperator.UnaryOperator
import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRUnaryOperation(target: IRVariable, ope: UnaryOperator, source: IRExpression) extends IRAbstractStatement {
    override def toString: String = s"$target=$ope $source"
}
