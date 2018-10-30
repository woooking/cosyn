package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression

class IRReturn(expr: Option[IRExpression], fromNode: Set[Node]) extends IRStatement(fromNode) {
    override def toString: String = s"return${expr.map(" " + _).mkString}"

    override def uses: Seq[IRExpression] = expr.toSeq

    init()
}
