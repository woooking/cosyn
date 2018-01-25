package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRArrayAccess(target: IRVariable, array: IRExpression, index: IRExpression) extends IRStatement {
    override def uses: Seq[IRExpression] = Seq(array, index)

    init()
}
