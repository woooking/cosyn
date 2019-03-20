package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

abstract class IRStatement(val fromNode: Set[Node]) {
    def uses: Seq[IRExpression]

    def init(): Unit = {
        uses.foreach(_.uses += this)
    }
}
