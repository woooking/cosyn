package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression

class IRBinaryOperation(cfg: CFGImpl, val ope: BinaryExpr.Operator, lhs: IRExpression, rhs: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target = $lhs $ope $rhs"

    override def uses: Seq[IRExpression] = Seq(lhs, rhs)

    init()
}
