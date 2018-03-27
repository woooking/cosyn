package com.github.woooking.cosyn.javaparser.stmt

import cats.instances.list._
import cats.instances.option._
import com.github.javaparser.ast.stmt.{SwitchEntryStmt => JPSwitchEntryStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._

class SwitchEntryStmt(override val delegate: JPSwitchEntryStmt) extends Statement {
    val label: Option[Expression[_]] = delegate.getLabel.asScala
    val statements: List[Statement] = delegate.getStatements.asScala.toList
}

object SwitchEntryStmt {
    def apply(delegate: JPSwitchEntryStmt): SwitchEntryStmt = new SwitchEntryStmt(delegate)

    def unapply(arg: SwitchEntryStmt): Option[(
        Option[Expression[_]],
            List[Statement]
        )] = Some((
        arg.label,
        arg.statements,
    ))
}