package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

class IRUnaryOperation(cfg: CFG, val ope: UnaryExpr.Operator, val source: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$ope $source"

    override def uses: Seq[IRExpression] = Seq(source)

    init()
}
