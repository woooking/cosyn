package com.github.woooking.cosyn.ir.statements

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRClassExpr(target: IRVariable, ty: Type) extends IRAbstractStatement {

}
