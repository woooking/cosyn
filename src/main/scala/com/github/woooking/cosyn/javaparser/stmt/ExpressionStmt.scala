package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{ExpressionStmt => JPExpressionStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

class ExpressionStmt(override val delegate: JPExpressionStmt) extends Statement {
    val expression: Expression[_] = Expression(delegate.getExpression)
}

object ExpressionStmt {
    def apply(delegate: JPExpressionStmt): ExpressionStmt = new ExpressionStmt(delegate)

    def unapply(arg: ExpressionStmt): Option[Expression[_]] = Some(arg.expression)
}