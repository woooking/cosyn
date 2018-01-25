package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.statements.UnaryOperator.UnaryOperator
import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRConditionalExpr(target: IRVariable,
                             condition: IRExpression,
                             thenExpr: IRExpression,
                             elseExpr: IRExpression) extends IRStatement {

    override def uses: Seq[IRExpression] = Seq(condition, thenExpr, elseExpr)

    init()
}
