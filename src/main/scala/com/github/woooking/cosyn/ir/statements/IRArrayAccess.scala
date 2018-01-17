package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRArrayAccess(target: IRVariable, array: IRExpression, index: IRExpression) extends IRAbstractStatement {
    addUse(array)
    addUse(index)
}
