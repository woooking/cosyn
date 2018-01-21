package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.{IRExpression, IRVariable}

trait IRStatement {
    def addUse(exp: IRExpression): Unit = exp match {
        case v: IRVariable => v.uses += this
        case _ =>
    }
}

abstract class IRDefStatement(cfg: CFG) extends IRStatement {
    val target: IRVariable
}

