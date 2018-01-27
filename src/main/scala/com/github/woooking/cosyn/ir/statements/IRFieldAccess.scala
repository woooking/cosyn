package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression

class IRFieldAccess(cfg: CFG, val receiver: IRExpression, val field: String) extends IRDefStatement(cfg) {
    override def toString: String = s"$target=$receiver.$field"

    override def uses: Seq[IRExpression] = Seq(receiver)

    init()
}

