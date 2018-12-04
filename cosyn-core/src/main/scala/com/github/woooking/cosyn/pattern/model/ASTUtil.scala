package com.github.woooking.cosyn.pattern.model

import com.github.woooking.cosyn.pattern.model.stmt.Statement

import scala.annotation.tailrec

object ASTUtil {
    @tailrec
    def getParentStmt(node: Node): Statement = {
        node match {
            case s: Statement => s
            case _ => getParentStmt(node.parent)
        }
    }
}
