package com.github.woooking.cosyn.code.model.stmt

import com.github.woooking.cosyn.code.model.Node
import com.github.woooking.cosyn.code.model.expr.Expression

case class ExprStmt(expr: Expression) extends Statement {
    expr.parent = this
    override def toString: String = s"$expr;"

    override def children: Seq[Node] = Seq(expr)
}

