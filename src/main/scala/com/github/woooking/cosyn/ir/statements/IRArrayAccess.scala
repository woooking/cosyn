package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression

class IRArrayAccess(cfg: CFG, array: IRExpression, index: IRExpression) extends IRDefStatement(cfg) {
    override def uses: Seq[IRExpression] = Seq(array, index)

    init()
}
