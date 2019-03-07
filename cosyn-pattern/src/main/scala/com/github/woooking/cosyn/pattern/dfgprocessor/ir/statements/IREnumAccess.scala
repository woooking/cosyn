package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.{IREnum, IRExpression}

class IREnumAccess(cfg: CFGImpl, val ty: String, val constant: IREnum, fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=$ty.$constant"

    override def uses: Seq[IRExpression] = Seq(constant)

    init()
}

