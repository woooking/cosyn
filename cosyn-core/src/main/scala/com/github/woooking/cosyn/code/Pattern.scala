package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.hole_resolver._
import com.github.woooking.cosyn.code.model.{BlockStmt, Expression, HoleExpr}
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.code.model.ty.BasicType
import com.github.woooking.cosyn.code.patterns.FillCellColor
import com.github.woooking.cosyn.config.Config

import scala.annotation.tailrec
import scala.io.StdIn

case class Pattern(stmts: BlockStmt, holes: Seq[HoleExpr]) {
    def fillHole(hole: HoleExpr, expr: Expression): Pattern = {

    }
}

object Pattern {
    val patterns = Map(
        "fill cell color" -> FillCellColor.pattern
    )

    @tailrec
    def processQA(context: Context, pattern: Pattern, hole: HoleExpr, qa: Question): Seq[HoleExpr] = {
        println(qa)
        val input = StdIn.readLine()
        qa.processInput(context, hole, input) match {
            case Right(newHoles) => newHoles
            case Left(q) => processQA(context, hole, q)
        }
    }


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
        println(qa(context, pattern).stmts.generateCode(""))
        KnowledgeGraph.close()
    }
}
