package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRAssignment(cfg: CFG, val value: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target = $value"

    override def uses: Seq[IRExpression] = Seq(value)

    init()
}
