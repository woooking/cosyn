package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.stmt.{ThrowStmt => JPThrowStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

class ThrowStmt(override val delegate: JPThrowStmt) extends Statement {
    val expression: Expression[_ <: Node] = delegate.getExpression
}

object ThrowStmt {
    def apply(delegate: JPThrowStmt): ThrowStmt = new ThrowStmt(delegate)

    def unapply(arg: ThrowStmt): Option[Expression[_ <: Node]] = Some(arg.expression)
}