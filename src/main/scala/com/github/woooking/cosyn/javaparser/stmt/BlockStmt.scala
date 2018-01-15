package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{BlockStmt => JPBlockStmt}

import scala.collection.JavaConverters._

class BlockStmt(override val delegate: JPBlockStmt) extends Statement {
    val statements: List[Statement] = delegate.getStatements.asScala.map(Statement.apply).toList
}

object BlockStmt {
    def apply(delegate: JPBlockStmt): BlockStmt = new BlockStmt(delegate)

    def unapply(arg: BlockStmt): Option[List[Statement]] = Some(arg.statements)
}