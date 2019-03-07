package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRExpression

class IRConditionalExpr(cfg: CFG,
                        condition: IRExpression,
                        thenExpr: IRExpression,
                        elseExpr: IRExpression, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {

    override def uses: Seq[IRExpression] = Seq(condition, thenExpr, elseExpr)

    init()
}
