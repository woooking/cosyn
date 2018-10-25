package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{EmptyStmt => JPEmptyStmt}

class EmptyStmt(override val delegate: JPEmptyStmt) extends Statement {
}

object EmptyStmt {
    def apply(delegate: JPEmptyStmt): EmptyStmt = new EmptyStmt(delegate)

    def unapply(arg: EmptyStmt): Boolean = true
}