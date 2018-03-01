package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRAssert(check: IRExpression, message: Option[IRExpression]) extends IRStatement {
    override def uses: Seq[IRExpression] = check +: message.toSeq

    init()
}
