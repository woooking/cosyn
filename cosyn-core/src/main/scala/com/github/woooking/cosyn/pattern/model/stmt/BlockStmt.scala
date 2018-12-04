package com.github.woooking.cosyn.pattern.model.stmt

class BlockStmt(var statements: Seq[Statement]) extends Statement {
    statements.foreach(_.parent = this)

    def replace(oldStmt: Statement, newStmts: Statement*): Unit = {
        val (front, end) = statements.span(_ != oldStmt)
        statements = front ++ newStmts ++ end.tail
    }

    override def toString: String = statements.mkString("\n")

    override def generateCode(indent: String): String = {
        statements.map(_.generateCode(indent)).mkString("\n")
    }
}

object BlockStmt {
    def apply(statements: Statement*): BlockStmt = new BlockStmt(statements.toSeq)
}
