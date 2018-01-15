package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRExpression, IRVariable}

case class IRFieldAccess(target: IRVariable, receiver: IRExpression, field: String) extends IRAbstractStatement {
    override def toString: String = s"$target=$receiver.$field"
}

