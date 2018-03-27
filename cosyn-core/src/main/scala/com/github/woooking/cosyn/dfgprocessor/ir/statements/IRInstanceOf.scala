package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.javaparser.ast.`type`.ReferenceType
import com.github.woooking.cosyn.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRInstanceOf(cfg: CFG, expression: IRExpression, val ty: ReferenceType, fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$expression instanceOf $ty"

    override def uses: Seq[IRExpression] = Seq(expression)

    init()
}
