package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRExpression

class IRThrow(exception: IRExpression, fromNode: Set[Node]) extends IRStatement(fromNode) {
    override def uses: Seq[IRExpression] = Seq(exception)

    init()
}