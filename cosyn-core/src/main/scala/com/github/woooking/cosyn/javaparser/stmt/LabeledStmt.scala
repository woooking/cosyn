package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{LabeledStmt => JPLabeledStmt}

class LabeledStmt(override val delegate: JPLabeledStmt) extends Statement {
    val label: String = delegate.getLabel
    val stmt: Statement = delegate.getStatement
}

object LabeledStmt {
    def apply(delegate: JPLabeledStmt): LabeledStmt = new LabeledStmt(delegate)

    def unapply(arg: LabeledStmt): Option[(String, Statement)] = Some((arg.label, arg.stmt))
}