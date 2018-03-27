package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.dfgprocessor.ir.IRTemp
import com.github.woooking.cosyn.javaparser.NodeDelegate

abstract class IRDefStatement(cfg: CFG, fromNode: Set[NodeDelegate[_]] = Set.empty) extends IRStatement(fromNode) {
    val target: IRTemp = cfg.createTempVar(this)
}
