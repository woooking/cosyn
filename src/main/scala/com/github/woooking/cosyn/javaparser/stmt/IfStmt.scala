package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{IfStmt => JPIfStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

class IfStmt(override val delegate: JPIfStmt) extends Statement {
    val condition: Expression[_] = Expression(delegate.getCondition)

    val thenStmt: Statement = Statement(delegate.getThenStmt)

    val elseStmt: Option[Statement] = delegate.getElseStmt.asScala.map(s => Statement(s))
}

object IfStmt {
    def apply(delegate: JPIfStmt): IfStmt = new IfStmt(delegate)

    def unapply(arg: IfStmt): Option[(
        Expression[_],
            Statement,
            Option[Statement]
        )] = Some((
        arg.condition,
        arg.thenStmt,
        arg.elseStmt
    ))
}