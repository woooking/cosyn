package com.github.woooking.cosyn.javaparser.stmt

import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.stmt.{
    Statement => JPStatement,
    BlockStmt => JPBlockStmt,
    ContinueStmt => JPContinueStmt,
    ExpressionStmt => JPExpressionStmt,
    ForeachStmt => JPForeachStmt,
    IfStmt => JPIfStmt,
    TryStmt => JPTryStmt,
    WhileStmt => JPWhileStmt,
}

trait Statement extends NodeDelegate[JPStatement]

object Statement {
    def apply(statement: JPStatement): Statement = statement match {
        case s: JPBlockStmt => BlockStmt(s)
        case s: JPContinueStmt => ContinueStmt(s)
        case s: JPExpressionStmt => ExpressionStmt(s)
        case s: JPForeachStmt => ForeachStmt(s)
        case s: JPIfStmt => IfStmt(s)
        case s: JPTryStmt => TryStmt(s)
        case s: JPWhileStmt => WhileStmt(s)
    }
}
