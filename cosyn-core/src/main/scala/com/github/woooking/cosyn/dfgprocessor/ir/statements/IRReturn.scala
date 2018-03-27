package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRReturn(expr: Option[IRExpression], fromNode: Set[NodeDelegate[_]]) extends IRStatement(fromNode) {
    override def toString: String = s"return${expr.map(" " + _).mkString}"

    override def uses: Seq[IRExpression] = expr.toSeq

    init()
}
