package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRTemp
import com.github.woooking.cosyn.javaparser.NodeDelegate

abstract class IRDefStatement(cfg: CFG, fromNode: Set[NodeDelegate[_]] = Set.empty) extends IRStatement(fromNode) {
    val target: IRTemp = cfg.createTempVar(this)
}
