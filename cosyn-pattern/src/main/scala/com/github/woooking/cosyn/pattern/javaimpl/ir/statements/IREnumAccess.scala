package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.{IREnum, IRExpression}

class IREnumAccess(cfg: CFG, val ty: String, val constant: IREnum, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$ty.$constant"

    override def uses: Seq[IRExpression] = Seq(constant)

    init()
}

