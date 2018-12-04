package com.github.woooking.cosyn.pattern.model.stmt

import com.github.woooking.cosyn.pattern.model.expr.Expression

class ExprStmt(expr: Expression) extends Statement {
    expr.parent = this
    override def toString: String = s"$expr;"
}

object ExprStmt {
    implicit def expr2stmt(expr: Expression): ExprStmt = ExprStmt(expr)

    def apply(expr: Expression): ExprStmt = new ExprStmt(expr)
}


