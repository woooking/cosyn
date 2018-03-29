package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{ContinueStmt => JPContinueStmt}
import com.github.woooking.cosyn.util.OptionConverters._

class ContinueStmt(override val delegate: JPContinueStmt) extends Statement {
    val label: Option[String] = delegate.getLabel.asScala.map(_.asString())
}

object ContinueStmt {
    def apply(delegate: JPContinueStmt): ContinueStmt = new ContinueStmt(delegate)

    def unapply(arg: ContinueStmt): Option[Option[String]] = Some(arg.label)
}