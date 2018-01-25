package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.statements.BinaryOperator.BinaryOperator
import com.github.woooking.cosyn.ir.{IRExpression, IRVariable}

case class IRBinaryOperation(target: CFG#IRTemp, ope: BinaryOperator, lhs: IRExpression, rhs: IRExpression) extends IRStatement {
    override def toString: String = s"$target=$lhs $ope $rhs"

    override def uses: Seq[IRExpression] = Seq(lhs, rhs)

    init()
}
