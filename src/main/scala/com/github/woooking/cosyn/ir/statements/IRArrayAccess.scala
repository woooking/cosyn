package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRArrayAccess(cfg: CFG, array: IRExpression, index: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target = $array[$index]"

    override def uses: Seq[IRExpression] = Seq(array, index)

    init()
}
