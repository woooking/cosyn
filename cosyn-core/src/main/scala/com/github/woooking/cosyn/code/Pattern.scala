package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.code.model.ty.BasicType
import com.github.woooking.cosyn.code.patterns.FillCellColor

case class Pattern(stmts: BlockStmt, holes: Seq[HoleExpr]) {
    def parentOf(node: Node): Node = ???

    def parentStmtOf(node: Node): Statement = ???

    def replaceStmtInBlock(block: BlockStmt, oldStmt: Statement, newStmts: Statement*): Pattern = ???

    def fillHole(hole: HoleExpr, expr: Expression): Pattern = {
        ???
    }
}

object Pattern {
    val patterns = Map(
        "fill cell color" -> FillCellColor.pattern
    )

    def main(args: Array[String]): Unit = {
        //        ---- case 1 ----
        val context = new Context(Seq("sheet" -> BasicType("org.apache.poi.ss.usermodel.Sheet")), Seq("java.lang.Object"))
//        context.variables += "wb" -> BasicType("org.apache.poi.hssf.usermodel.HSSFWorkbook")
        val pattern = FillCellColor.pattern
        //        ---- case 2 ----
        //        val context = new Context(Seq("java.lang.Object"))
        //        context.variables += "wb" -> "org.apache.poi.hssf.usermodel.HSSFWorkbook"
        //        val pattern = CreateHyperlink.pattern
        //         ---- case 3 ----
        //        val context = new Context(Seq("java.lang.Object"))
        //        context.variables += "sheet" -> BasicType("org.apache.poi.hssf.usermodel.HSSFSheet")
        //        val pattern = CreateConditionalFormatting.pattern

        context.update(pattern)
        KnowledgeGraph.close()
    }
}
