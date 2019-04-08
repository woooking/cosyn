package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.ReferenceType
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

class IRInstanceOf(cfg: CFG, expression: IRExpression, val ty: ReferenceType, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$expression instanceOf $ty"

    override def uses: Seq[IRExpression] = Seq(expression)

    init()
}
