package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRExpression

class IRAssignment(cfg: CFGImpl, val value: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target = $value"

    override def uses: Seq[IRExpression] = Seq(value)

    init()
}