package com.github.woooking.cosyn.pattern.model.stmt

import com.github.woooking.cosyn.pattern.model.expr.Expression
import com.github.woooking.cosyn.util.CodeUtil

class ForEachStmt(ty: String, variable: String, iterable: Expression, block: BlockStmt) extends Statement {
    iterable.parent = this
    block.parent = this

    override def generateCode(indent: String): String =
        s"""${indent}for (${CodeUtil.qualifiedClassName2Simple(ty)} $variable : $iterable) {
           |${block.generateCode(s"    $indent")}
           |$indent}""".stripMargin

    override def toString: String =
        s"""for (${CodeUtil.qualifiedClassName2Simple(ty)} $variable : $iterable) {
           |    $block
           |}""".stripMargin
}

object ForEachStmt {
    def apply(ty: String, variable: String, iterable: Expression, block: BlockStmt): ForEachStmt = new ForEachStmt(ty, variable, iterable, block)
}




