package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRAssignment(target: IRExpression, source: IRExpression) extends IRAbstractStatement {
    override def toString: String = s"$target=$source"
}
