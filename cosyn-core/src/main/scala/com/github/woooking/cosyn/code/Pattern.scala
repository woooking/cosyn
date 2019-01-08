package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.code.patterns.{ChangeFontFamily, FillCellColor}

import scala.annotation.tailrec

case class Pattern(stmts: BlockStmt, holes: Seq[HoleExpr]) {
    private val parentMap = ParentCollector[BlockStmt].collect(null, stmts)

    def parentOf(node: Node): Node = parentMap(node)

    @tailrec
    final def parentStmtOf(node: Node): Statement = parentOf(node) match {
        case s: Statement => s
        case n => parentStmtOf(n)
    }

    def replaceStmtInBlock(block: BlockStmt, oldStmt: Statement, newStmts: Statement*): Pattern = ???

    def fillHole(hole: HoleExpr, expr: Expression): Pattern = {
        ???
    }
}

object Pattern {
    val patterns = Map(
        "fill cell color" -> FillCellColor.pattern,
        "change font family" -> ChangeFontFamily.pattern,
    )

    //        ---- case 2 ----
    //        val context = new Context(Seq("java.lang.Object"))
    //        context.variables += "wb" -> "org.apache.poi.hssf.usermodel.HSSFWorkbook"
    //        val pattern = CreateHyperlink.pattern
    //         ---- case 3 ----
    //        val context = new Context(Seq("java.lang.Object"))
    //        context.variables += "sheet" -> BasicType("org.apache.poi.hssf.usermodel.HSSFSheet")
    //        val pattern = CreateConditionalFormatting.pattern
}
