package com.github.woooking.cosyn.ir.statements

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.IRExpression

class IRArrayCreation(cfg: CFG, ty: Type, size: Seq[IRExpression], initializers: Seq[IRExpression]) extends IRDefStatement(cfg) {
    override def uses: Seq[IRExpression] = size ++ initializers

    init()
}
