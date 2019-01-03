package com.github.woooking.cosyn.code.model

import com.github.woooking.cosyn.code.model.expr.Expression
import com.github.woooking.cosyn.code.model.stmt.{BlockStmt, ExprStmt, ForEachStmt, Statement}

trait CodeBuilder {
    def block(statements: Statement*): BlockStmt = {
        BlockStmt(statements.toSeq)
    }

    def foreach(ty: String, variable: String, iterable: Expression, block: BlockStmt): ForEachStmt = ForEachStmt(ty, variable, iterable, block)
}

object CodeBuilder extends CodeBuilder {
    implicit def expr2stmt(expr: Expression): ExprStmt = ExprStmt(expr)
}
