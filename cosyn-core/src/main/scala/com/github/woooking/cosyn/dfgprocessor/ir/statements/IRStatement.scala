package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

abstract class IRStatement(val fromNode: Set[NodeDelegate[_]]) {
    def uses: Seq[IRExpression]

    def init(): Unit = {
        uses.foreach(_.uses += this)
    }
}
