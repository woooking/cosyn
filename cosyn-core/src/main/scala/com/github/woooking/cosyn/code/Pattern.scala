package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model.visitors.{FillHoleVisitor, HoleCollector, ParentCollector}
import com.github.woooking.cosyn.code.model.{Expression, _}
import com.github.woooking.cosyn.code.patterns.{ChangeFontFamily, FillCellColor}

import scala.annotation.tailrec

case class Pattern(stmts: BlockStmt) {
    private val parentMap = ParentCollector.instance[BlockStmt].collect(null, stmts)

    lazy val holes: List[HoleExpr] = HoleCollector.instance[BlockStmt].collect(stmts)

    def parentOf(node: Node): Node = parentMap(node)

    @tailrec
    final def parentStmtOf(node: Node): Statement = parentOf(node) match {
        case s: Statement => s
        case n => parentStmtOf(n)
    }

    def replaceStmtInBlock(block: BlockStmt, oldStmt: Statement, newStmts: Statement*): Pattern = ???

    def fillHole(hole: HoleExpr, expr: Expression): Pattern = {
        FillHoleVisitor.instance[BlockStmt].fill(stmts, hole, expr) match {
            case None => this
            case Some(b) => this.copy(stmts = b)
        }
    }
}

object Pattern {
    val patterns: Map[String, Pattern] = Map(
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
