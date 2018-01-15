package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{BreakStmt => JPBreakStmt}
import com.github.woooking.cosyn.util.OptionConverters._

class BreakStmt(override val delegate: JPBreakStmt) extends Statement {
    val label: Option[String] = delegate.getLabel.asScala.map(_.asString())
}

object BreakStmt {
    def apply(delegate: JPBreakStmt): BreakStmt = new BreakStmt(delegate)

    def unapply(arg: BreakStmt): Option[Option[String]] = Some(arg.label)
}