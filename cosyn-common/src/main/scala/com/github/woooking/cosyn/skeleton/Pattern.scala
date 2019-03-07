package com.github.woooking.cosyn.skeleton

import com.github.woooking.cosyn.patterns._
import com.github.woooking.cosyn.skeleton.model.{Expression, _}
import com.github.woooking.cosyn.skeleton.visitors.{FillHoleVisitor, HoleCollector, ParentCollector, ReplaceStmtVisitor}

import scala.annotation.tailrec

case class Pattern(holeFactory: HoleFactory, stmts: BlockStmt) {
    private val parentMap = ParentCollector.instance[BlockStmt].collect(null, stmts)

    lazy val holes: List[HoleExpr] = HoleCollector.instance[BlockStmt].collect(stmts)

    def parentOf(node: Node): Node = parentMap(node)

    @tailrec
    final def parentStmtOf(node: Node): Statement = parentOf(node) match {
        case s: Statement => s
        case n => parentStmtOf(n)
    }

    def replaceStmtInBlock(block: BlockStmt, oldStmt: Statement, newStmts: Statement*): Pattern = {
        ReplaceStmtVisitor.instance[BlockStmt].replace(stmts, block, oldStmt, newStmts) match {
            case None => this
            case Some(b) => this.copy(stmts = b)
        }
    }

    def fillHole(hole: HoleExpr, expr: Expression): Pattern = {
        copy(stmts = FillHoleVisitor.fillHole(stmts, hole, expr))
    }
}

object Pattern {
    val patterns: Map[String, Pattern] = Map(
        "" -> TestPattern.pattern,
        "fill cell color" -> FillCellColor.pattern,
        "change font family" -> ChangeFontFamily.pattern,
        "create conditional formatting" -> CreateConditionalFormatting.pattern,
        "create drop down list" -> CreateDropDownList.pattern,
        "create hyper link" -> CreateHyperlink.pattern,
    )
}
