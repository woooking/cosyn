package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRExpression, IRVariable}

trait IRAbstractStatement {
    def addUse(exp: IRExpression): Unit = exp match {
        case v: IRVariable => v.uses += this
        case _ =>
    }

}
