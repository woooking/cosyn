package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.stmt.{DoStmt => JPDoStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

class DoStmt(override val delegate: JPDoStmt) extends Statement {
    val body: Statement = Statement(delegate.getBody)
    val expression: Expression[_ <: Node] = delegate.getCondition
}

object DoStmt {
    def apply(delegate: JPDoStmt): DoStmt = new DoStmt(delegate)

    def unapply(arg: DoStmt): Option[(
        Statement,
            Expression[_ <: Node]
        )] = Some((
        arg.body,
        arg.expression
    ))
}