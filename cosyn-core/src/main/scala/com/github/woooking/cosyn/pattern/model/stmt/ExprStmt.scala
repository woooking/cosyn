package com.github.woooking.cosyn.pattern.model.stmt

import com.github.woooking.cosyn.pattern.model.expr.Expression

case class ExprStmt(expr: Expression) extends Statement

object ExprStmt {
    implicit def expr2stmt(expr: Expression): ExprStmt = ExprStmt(expr)
}


