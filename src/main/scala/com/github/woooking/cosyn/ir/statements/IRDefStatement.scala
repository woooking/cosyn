package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRTemp

abstract class IRDefStatement(cfg: CFG) extends IRStatement {
    val target: IRTemp = cfg.createTempVar()
}
