package com.github.woooking.cosyn.ir.statements

import com.github.javaparser.ast.expr.{AssignExpr, BinaryExpr}

object UnaryOperator extends Enumeration {
    type UnaryOperator = Value

    val classOf: UnaryOperator = Value("classOf")

}
