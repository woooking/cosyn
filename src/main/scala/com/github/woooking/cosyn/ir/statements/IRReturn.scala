package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRReturn(expr: Option[IRExpression]) extends IRAbstractStatement {
    override def toString: String = s"return${expr.map(" " + _).mkString}"
    expr.foreach(addUse)
}
