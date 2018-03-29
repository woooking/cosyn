package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{SwitchStmt => JPSwitchStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression

import scala.collection.JavaConverters._

class SwitchStmt(override val delegate: JPSwitchStmt) extends Statement {
    val selector: Expression[_] = delegate.getSelector
    val entries: List[SwitchEntryStmt] = delegate.getEntries.asScala.map(SwitchEntryStmt.apply).toList
}

object SwitchStmt {
    def apply(delegate: JPSwitchStmt): SwitchStmt = new SwitchStmt(delegate)

    def unapply(arg: SwitchStmt): Option[(
        Expression[_],
            List[SwitchEntryStmt]
        )] = Some((
        arg.selector,
        arg.entries
    ))
}