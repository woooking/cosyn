package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern.hole_resolver._
import com.github.woooking.cosyn.pattern.model.expr.HoleExpr
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern.model.ty.BasicType
import com.github.woooking.cosyn.pattern.patterns.{CreateConditionalFormatting, CreateHyperlink, FillCellColor}

import scala.annotation.tailrec
import scala.io.StdIn

case class Pattern(stmts: BlockStmt, holes: Seq[HoleExpr]) {

}

object Pattern {
    val resolver = CombineHoleResolver(
        new EnumConstantHoleResolver,
        new StaticFieldAccessHoleResolver,
        new ReceiverHoleResolver,
        new ArgumentHoleResolver,
        new VariableInitializationHoleResolver,
    )

    val patterns = Map(
        "fill cell color" -> FillCellColor.pattern
    )

    @tailrec
    def processQA(context: Context, hole: HoleExpr, qa: QA): Seq[HoleExpr] = {
        println(qa)
        val input = StdIn.readLine()
        qa.processInput(context, hole, input) match {
            case Right(newHoles) => newHoles
            case Left(q) => processQA(context, hole, q)
        }
    }

    @tailrec
    def qa(context: Context, pattern: Pattern): Pattern = {
        println("-----")
        println(pattern.stmts.generateCode(""))
        println("-----")
        pattern.holes.toList match {
            case Nil => pattern
            case hole :: tails =>
                resolver.resolve(FillCellColor.pattern.stmts, hole, context) match {
                    case Some(q) =>
                        val newHoles = processQA(context, hole, q)
                        qa(context, Pattern(pattern.stmts, newHoles ++ tails))
                    case None => ???
                }
        }
    }

    def main(args: Array[String]): Unit = {
        //        ---- case 1 ----
        //        val context = new Context(Seq("java.lang.Object"))
        //        context.variables += "wb" -> "org.apache.poi.hssf.usermodel.HSSFWorkbook"
        //        println(qa(context, FillCellColor.pattern).stmts.generateCode(""))
        //        ---- case 2 ----
        //        val context = new Context(Seq("java.lang.Object"))
        //        context.variables += "wb" -> "org.apache.poi.hssf.usermodel.HSSFWorkbook"
        //        val pattern = CreateHyperlink.pattern
        //         ---- case 3 ----
        val context = new Context(Seq("java.lang.Object"))
        context.variables += "sheet" -> BasicType("org.apache.poi.hssf.usermodel.HSSFSheet")
        val pattern = CreateConditionalFormatting.pattern

        context.update(pattern)
        println(qa(context, pattern).stmts.generateCode(""))
        KnowledgeGraph.close()
    }
}
