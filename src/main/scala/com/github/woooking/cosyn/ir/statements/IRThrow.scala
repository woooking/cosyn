package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRThrow(exception: IRExpression) extends IRStatement(Set.empty) {
    override def uses: Seq[IRExpression] = Seq(exception)

    init()
}
