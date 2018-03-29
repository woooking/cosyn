package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRArrayAccess(cfg: CFGImpl, array: IRExpression, index: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {

    override def toString: String = s"$target = $array[$index]"

    override def uses: Seq[IRExpression] = Seq(array, index)

    init()
}

