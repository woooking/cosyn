package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{AssertStmt => JPAssertStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._
import cats.instances.option._

class AssertStmt(override val delegate: JPAssertStmt) extends Statement {
    val check: Expression[_] = delegate.getCheck
    val message: Option[Expression[_]] = delegate.getMessage.asScala
}

object AssertStmt {
    def apply(delegate: JPAssertStmt): AssertStmt = new AssertStmt(delegate)

    def unapply(arg: AssertStmt): Option[(Expression[_], Option[Expression[_]])] = Some((arg.check, arg.message))
}