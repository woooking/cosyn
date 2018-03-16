package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRConditionalExpr(cfg: CFG,
                             condition: IRExpression,
                             thenExpr: IRExpression,
                             elseExpr: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {

    override def uses: Seq[IRExpression] = Seq(condition, thenExpr, elseExpr)

    init()
}
