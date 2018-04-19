package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{AssertStmt => JPAssertStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._
import cats.instances.option._
import com.github.javaparser.ast.Node

class AssertStmt(override val delegate: JPAssertStmt) extends Statement {
    val check: Expression[_<: Node] = delegate.getCheck
    val message: Option[Expression[_<: Node]] = delegate.getMessage.asScala
}

object AssertStmt {
    def apply(delegate: JPAssertStmt): AssertStmt = new AssertStmt(delegate)

    def unapply(arg: AssertStmt): Option[(Expression[_<: Node], Option[Expression[_<: Node]])] = Some((arg.check, arg.message))
}