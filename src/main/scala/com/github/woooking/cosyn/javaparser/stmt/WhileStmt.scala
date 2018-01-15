package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{WhileStmt => JPWhileStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

class WhileStmt(override val delegate: JPWhileStmt) extends Statement {
    val condition: Expression = Expression(delegate.getCondition)

    val body: Statement = Statement(delegate.getBody)
}

object WhileStmt {
    def apply(delegate: JPWhileStmt): WhileStmt = new WhileStmt(delegate)

    def unapply(arg: WhileStmt): Option[(Expression, Statement)] = Some((arg.condition, arg.body))
}