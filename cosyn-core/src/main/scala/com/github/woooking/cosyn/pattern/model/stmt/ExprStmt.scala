package com.github.woooking.cosyn.pattern.model.stmt

import com.github.woooking.cosyn.pattern.model.Node
import com.github.woooking.cosyn.pattern.model.expr.Expression

case class ExprStmt(expr: Expression) extends Statement {
    expr.parent = this
    override def toString: String = s"$expr;"

    override def children: Seq[Node] = Seq(expr)
}

object ExprStmt {
    implicit def expr2stmt(expr: Expression): ExprStmt = ExprStmt(expr)

    def apply(expr: Expression): ExprStmt = new ExprStmt(expr)
}


