package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.ir.statements.BinaryOperator.BinaryOperator

class IRBinaryOperation(cfg: CFG, val ope: BinaryOperator, lhs: IRExpression, rhs: IRExpression) extends IRDefStatement(cfg) {
    override def toString: String = s"$target=$lhs $ope $rhs"

    override def uses: Seq[IRExpression] = Seq(lhs, rhs)

    init()
}
