package com.github.woooking.cosyn.code.model.stmt

import com.github.woooking.cosyn.code.model.Node
import com.github.woooking.cosyn.code.model.expr.Expression
import com.github.woooking.cosyn.util.CodeUtil

case class ForEachStmt(ty: String, variable: String, iterable: Expression, block: BlockStmt) extends Statement {
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

    override def children: Seq[Node] = Seq(iterable, block)
}





