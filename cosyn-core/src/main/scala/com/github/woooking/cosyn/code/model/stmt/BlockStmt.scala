package com.github.woooking.cosyn.code.model.stmt

import com.github.woooking.cosyn.code.model.Node

case class BlockStmt(var statements: Seq[Statement]) extends Statement {
    statements.foreach(_.parent = this)

    def replace(oldStmt: Statement, newStmts: Statement*): Unit = {
        val (front, end) = statements.span(_ != oldStmt)
        statements = front ++ newStmts ++ end.tail
    }

    override def toString: String = statements.mkString("\n")

    override def generateCode(indent: String): String = {
        statements.map(_.generateCode(indent)).mkString("\n")
    }

    override def children: Seq[Node] = statements
}
