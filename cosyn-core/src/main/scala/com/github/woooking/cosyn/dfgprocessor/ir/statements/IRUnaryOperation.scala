package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.statements.UnaryOperator.UnaryOperator
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRUnaryOperation(cfg: CFGImpl, val ope: UnaryOperator, val source: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$ope $source"

    override def uses: Seq[IRExpression] = Seq(source)

    init()
}
