package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRAssignment(target: IRExpression, source: IRExpression) extends IRStatement {
    override def toString: String = s"$target=$source"
    addUse(source)
}
