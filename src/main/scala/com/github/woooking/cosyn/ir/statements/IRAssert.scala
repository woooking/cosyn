package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRAssert(condition: IRExpression, message: Option[IRExpression]) extends IRStatement {
    override def uses: Seq[IRExpression] = condition +: message.toSeq

    init()
}
