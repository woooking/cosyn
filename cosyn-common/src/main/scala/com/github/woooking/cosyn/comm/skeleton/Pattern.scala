package com.github.woooking.cosyn.comm.skeleton

import com.github.woooking.cosyn.comm.patterns._
import com.github.woooking.cosyn.comm.skeleton.model.{Expression, _}
import com.github.woooking.cosyn.comm.skeleton.visitors.{FillHoleVisitor, HoleCollector, ParentCollector, ReplaceStmtVisitor}
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import scala.annotation.tailrec

case class Pattern private(holeFactory: HoleFactory, stmts: BlockStmt, parentMap: Map[Node, Node], holes: List[HoleExpr]) {
    def parentOf(node: Node): Node = parentMap(node)

    @tailrec
    final def parentStmtOf(node: Node): Statement = parentOf(node) match {
        case s: Statement => s
        case n => parentStmtOf(n)
    }

    def replaceStmtInBlock(block: BlockStmt, oldStmt: Statement, newStmts: Statement*): Pattern = profile("replace-stmt") {
        ReplaceStmtVisitor.instance[BlockStmt].replace(stmts, block, oldStmt, newStmts) match {
            case None => this
            case Some(b) =>
                Pattern(holeFactory, b)
//                this.copy(stmts = b)
        }
    }

//    def fillHole(hole: HoleExpr, expr: Expression): Pattern = profile("fill-hole") {
//        val newStmts = FillHoleVisitor.fillHole(stmts, hole, expr)
//        val exprParentMap = ParentCollector.instance[Expression].collect(parentMap(hole), expr)
//        val exprHoles = HoleCollector.instance[Expression].collect(expr)
//        val newParentMap = parentMap - hole ++ exprParentMap
//        val newHoles = exprHoles ++ holes diff (hole :: Nil)
//        copy(stmts = newStmts, parentMap = newParentMap, holes = newHoles)
//    }

    def fillHole(hole: HoleExpr, expr: Expression): Pattern = profile("fill-hole") {
        val newStmts = FillHoleVisitor.fillHole(stmts, hole, expr)
        Pattern(holeFactory, newStmts)
    }
}

object Pattern {
    def apply(holeFactory: HoleFactory, stmts: BlockStmt): Pattern = {
        val parentMap = ParentCollector.instance[BlockStmt].collect(null, stmts)

        val holes: List[HoleExpr] = HoleCollector.instance[BlockStmt].collect(stmts)

        Pattern(holeFactory, stmts, parentMap, holes)
    }

    val patterns: Map[String, Pattern] = Map(
        "" -> TestPattern.pattern,
        "fill cell color" -> FillCellColor.pattern,
        "change font family" -> ChangeFontFamily.pattern,
        "create conditional formatting" -> CreateConditionalFormatting.pattern,
        "create drop down list" -> CreateDropDownList.pattern,
        "create hyper link" -> CreateHyperlink.pattern,
    )
}
