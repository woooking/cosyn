package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRAssert(check: IRExpression, message: Option[IRExpression], fromNode: Set[NodeDelegate[_]]) extends IRStatement(fromNode) {
    override def uses: Seq[IRExpression] = check +: message.toSeq

    init()
}
