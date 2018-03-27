package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRArrayCreation(cfg: CFG, ty: Type, size: Seq[IRExpression], initializers: Seq[IRExpression], fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def uses: Seq[IRExpression] = size ++ initializers

    init()
}
