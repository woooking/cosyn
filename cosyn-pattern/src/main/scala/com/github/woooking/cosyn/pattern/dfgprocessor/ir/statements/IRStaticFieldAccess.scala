package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.{IRExpression, IRTypeObject}

class IRStaticFieldAccess(cfg: CFG, val receiver: IRTypeObject, val field: String, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$receiver.$field"

    override def uses: Seq[IRExpression] = Seq(receiver)

    init()
}

