package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.statements.UnaryOperator.UnaryOperator
import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRUnaryOperation(cfg: CFG, val ope: UnaryOperator, val source: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$ope $source"

    override def uses: Seq[IRExpression] = Seq(source)

    init()
}
