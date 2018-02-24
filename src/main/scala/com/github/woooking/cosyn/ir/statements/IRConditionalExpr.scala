package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression

case class IRConditionalExpr(cfg: CFG,
                             condition: IRExpression,
                             thenExpr: IRExpression,
                             elseExpr: IRExpression) extends IRDefStatement(cfg) {

    override def uses: Seq[IRExpression] = Seq(condition, thenExpr, elseExpr)

    init()
}
