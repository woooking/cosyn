package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRTemp

abstract class IRDefStatement(cfg: CFG, fromNode: Set[Node] = Set.empty) extends IRStatement(fromNode) {
    val target: IRTemp = cfg.createTempVar(this)
}
