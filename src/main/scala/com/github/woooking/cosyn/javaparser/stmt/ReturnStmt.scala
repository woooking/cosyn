package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{ReturnStmt => JPReturnStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

class ReturnStmt(override val delegate: JPReturnStmt) extends Statement {
    val expression: Option[Expression[_]] = delegate.getExpression.asScala.map(e => Expression(e))
}

object ReturnStmt {
    def apply(delegate: JPReturnStmt): ReturnStmt = new ReturnStmt(delegate)

    def unapply(arg: ReturnStmt): Option[Option[Expression[_]]] = Some(arg.expression)
}