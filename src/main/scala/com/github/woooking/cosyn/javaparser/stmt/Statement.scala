package com.github.woooking.cosyn.javaparser.stmt

import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.stmt.{
    Statement => JPStatement,
    BlockStmt => JPBlockStmt,
    TryStmt => JPTryStmt,
}

trait Statement extends NodeDelegate[JPStatement]

object Statement {
    def apply(statement: JPStatement): Statement = statement match {
        case s: JPBlockStmt => BlockStmt(s)
        case s: JPTryStmt => TryStmt(s)
    }
}
