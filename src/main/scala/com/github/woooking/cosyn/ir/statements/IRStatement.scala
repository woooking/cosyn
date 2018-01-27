package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.IRExpression

trait IRStatement {
    def uses: Seq[IRExpression]

    def init(): Unit = {
        uses.foreach(_.uses += this)
    }
}
