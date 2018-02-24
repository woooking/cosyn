package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.statements.UnaryOperator.UnaryOperator
import com.github.woooking.cosyn.ir.IRExpression

class IRUnaryOperation(cfg: CFG, val ope: UnaryOperator, val source: IRExpression) extends IRDefStatement(cfg) {
    override def toString: String = s"$target=$ope $source"

    override def uses: Seq[IRExpression] = Seq(source)

    init()
}
