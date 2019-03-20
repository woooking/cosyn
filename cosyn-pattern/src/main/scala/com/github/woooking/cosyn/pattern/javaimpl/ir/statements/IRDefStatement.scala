package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRTemp

abstract class IRDefStatement(cfg: CFG, fromNode: Set[Node] = Set.empty) extends IRStatement(fromNode) {
    val target: IRTemp = cfg.createTempVar(this)
}
