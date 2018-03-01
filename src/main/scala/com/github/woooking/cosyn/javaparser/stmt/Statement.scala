package com.github.woooking.cosyn.javaparser.stmt

import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.stmt.{
    Statement => JPStatement,
    AssertStmt => JPAssertStmt,
    BlockStmt => JPBlockStmt,
    BreakStmt => JPBreakStmt,
    ContinueStmt => JPContinueStmt,
    DoStmt => JPDoStmt,
    EmptyStmt => JPEmptyStmt,
    ExplicitConstructorInvocationStmt => JPExplicitConstructorInvocationStmt,
    ExpressionStmt => JPExpressionStmt,
    ForStmt => JPForStmt,
    ForeachStmt => JPForeachStmt,
    IfStmt => JPIfStmt,
    LabeledStmt => JPLabeledStmt,
    ReturnStmt => JPReturnStmt,
    SwitchStmt => JPSwitchStmt,
    SynchronizedStmt => JPSynchronizedStmt,
    ThrowStmt => JPThrowStmt,
    TryStmt => JPTryStmt,
    WhileStmt => JPWhileStmt,
}

trait Statement extends NodeDelegate[JPStatement]

object Statement {
    def apply(statement: JPStatement): Statement = statement match {
        case s: JPAssertStmt => AssertStmt(s)
        case s: JPBlockStmt => BlockStmt(s)
        case s: JPBreakStmt => BreakStmt(s)
        case s: JPContinueStmt => ContinueStmt(s)
        case s: JPDoStmt => DoStmt(s)
        case s: JPEmptyStmt => EmptyStmt(s)
        case s: JPExplicitConstructorInvocationStmt => ExplicitConstructorInvocationStmt(s)
        case s: JPExpressionStmt => ExpressionStmt(s)
        case s: JPForStmt => ForStmt(s)
        case s: JPForeachStmt => ForeachStmt(s)
        case s: JPIfStmt => IfStmt(s)
        case s: JPLabeledStmt => LabeledStmt(s)
        case s: JPReturnStmt => ReturnStmt(s)
        case s: JPSwitchStmt => SwitchStmt(s)
        case s: JPSynchronizedStmt => SynchronizedStmt(s)
        case s: JPThrowStmt => ThrowStmt(s)
        case s: JPTryStmt => TryStmt(s)
        case s: JPWhileStmt => WhileStmt(s)
    }
}
