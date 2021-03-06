package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.dfgprocessor.ir.statements.BinaryOperator.BinaryOperator
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRBinaryOperation(cfg: CFGImpl, val ope: BinaryOperator, lhs: IRExpression, rhs: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target = $lhs $ope $rhs"

    override def uses: Seq[IRExpression] = Seq(lhs, rhs)

    init()
}
