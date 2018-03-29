package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.{ForeachStmt => JPForeachStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

class ForeachStmt(override val delegate: JPForeachStmt) extends Statement {
    val variable: VariableDeclarationExpr = delegate.getVariable
    val iterable: Expression[_] = Expression(delegate.getIterable)
    val body: Statement = Statement(delegate.getBody)
}

object ForeachStmt {
    def apply(delegate: JPForeachStmt): ForeachStmt = new ForeachStmt(delegate)

    def unapply(arg: ForeachStmt): Option[(
        VariableDeclarationExpr,
            Expression[_],
            Statement
        )] = Some((
        arg.variable,
        arg.iterable,
        arg.body
    ))
}