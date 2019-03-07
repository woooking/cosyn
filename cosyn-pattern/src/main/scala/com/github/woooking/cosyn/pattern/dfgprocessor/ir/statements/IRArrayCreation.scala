package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRExpression

class IRArrayCreation(cfg: CFG, ty: Type, size: Seq[IRExpression], initializers: Seq[IRExpression], fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def uses: Seq[IRExpression] = size ++ initializers

    init()
}
