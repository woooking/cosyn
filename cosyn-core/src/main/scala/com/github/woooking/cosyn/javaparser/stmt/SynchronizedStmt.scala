package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.stmt.{SynchronizedStmt => JPSynchronizedStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

class SynchronizedStmt(override val delegate: JPSynchronizedStmt) extends Statement {
    val expression: Expression[_ <: Node] = delegate.getExpression
    val body: Statement = Statement(delegate.getBody)
}

object SynchronizedStmt {
    def apply(delegate: JPSynchronizedStmt): SynchronizedStmt = new SynchronizedStmt(delegate)

    def unapply(arg: SynchronizedStmt): Option[(
        Expression[_ <: Node],
            Statement
        )] = Some((
        arg.expression,
        arg.body
    ))
}