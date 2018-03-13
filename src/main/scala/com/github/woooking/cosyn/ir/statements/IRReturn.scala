package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

case class IRReturn(expr: Option[IRExpression]) extends IRStatement(Set.empty) {
    override def toString: String = s"return${expr.map(" " + _).mkString}"

    override def uses: Seq[IRExpression] = expr.toSeq

    init()
}
