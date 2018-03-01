package com.github.woooking.cosyn.ir.statements

import com.github.javaparser.ast.`type`.ReferenceType
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression

case class IRInstanceOf(cfg: CFG, expression: IRExpression, ty: ReferenceType) extends IRDefStatement(cfg) {
    override def toString: String = s"$target=$expression instanceOf $ty"

    override def uses: Seq[IRExpression] = Seq(expression)

    init()
}
