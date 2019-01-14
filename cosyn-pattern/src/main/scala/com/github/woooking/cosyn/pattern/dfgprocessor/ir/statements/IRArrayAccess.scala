package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRExpression

class IRArrayAccess(cfg: CFGImpl, array: IRExpression, index: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {

    override def toString: String = s"$target = $array[$index]"

    override def uses: Seq[IRExpression] = Seq(array, index)

    init()
}
