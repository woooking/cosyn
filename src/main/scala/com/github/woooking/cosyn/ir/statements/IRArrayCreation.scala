package com.github.woooking.cosyn.ir.statements

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.ir.{IRVariable, IRExpression}

case class IRArrayCreation(target: IRVariable,
                           ty: Type,
                           size: Seq[IRExpression],
                           initializers: Seq[IRExpression]) extends IRStatement {
    override def uses: Seq[IRExpression] = size ++ initializers

    init()
}
