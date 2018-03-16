package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRThrow(exception: IRExpression, fromNode: Set[NodeDelegate[_]]) extends IRStatement(fromNode) {
    override def uses: Seq[IRExpression] = Seq(exception)

    init()
}
