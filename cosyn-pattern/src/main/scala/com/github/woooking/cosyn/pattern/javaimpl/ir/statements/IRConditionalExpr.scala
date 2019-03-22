package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

class IRConditionalExpr(cfg: CFG,
                        val ty: String,
                        condition: IRExpression,
                        thenExpr: IRExpression,
                        elseExpr: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {

    override def uses: Seq[IRExpression] = Seq(condition, thenExpr, elseExpr)

    init()
}
