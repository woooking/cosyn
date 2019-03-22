package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

class IRArrayCreation(cfg: CFG, val ty: Type, size: Seq[IRExpression], initializers: Seq[IRExpression], fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def uses: Seq[IRExpression] = size ++ initializers

    init()
}
