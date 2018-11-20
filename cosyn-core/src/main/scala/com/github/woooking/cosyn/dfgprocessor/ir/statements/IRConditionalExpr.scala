package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression

class IRConditionalExpr(cfg: CFGImpl,
                        condition: IRExpression,
                        thenExpr: IRExpression,
                        elseExpr: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {

    override def uses: Seq[IRExpression] = Seq(condition, thenExpr, elseExpr)

    init()
}
