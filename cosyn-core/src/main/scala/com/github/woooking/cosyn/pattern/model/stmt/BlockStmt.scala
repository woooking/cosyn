package com.github.woooking.cosyn.pattern.model.stmt

class BlockStmt(statements: Seq[Statement]) extends Statement {
    override def toString: String = statements.mkString("\n")
}

object BlockStmt {
    def apply(statements: Statement*): BlockStmt = {
        val blockStmt = new BlockStmt(statements.toSeq)
        statements.foreach(_.parent = blockStmt)
        blockStmt
    }
}
