package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRAssignment(target: IRVariable, source: IRExpression) extends IRAbstractStatement {

}
