package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRExpression, IRVariable}

trait IRStatement {
    def uses: Seq[IRExpression]

    def init(): Unit = {
        uses.foreach {
            case v: IRVariable => v.uses += this
            case _ =>
        }
    }
}
