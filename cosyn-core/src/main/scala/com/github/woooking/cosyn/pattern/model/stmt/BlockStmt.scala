package com.github.woooking.cosyn.pattern.model.stmt

case class BlockStmt(statements: Seq[Statement]) extends Statement

object BlockStmt {
    def apply(statements: Statement*): BlockStmt = new BlockStmt(statements)
}
