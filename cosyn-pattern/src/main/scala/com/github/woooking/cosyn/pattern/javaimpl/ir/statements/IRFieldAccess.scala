package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

class IRFieldAccess(cfg: CFG, val receiver: IRExpression, val field: String, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$receiver.$field"

    override def uses: Seq[IRExpression] = Seq(receiver)

    init()
}

