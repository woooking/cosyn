package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, NameExpr}
import com.github.woooking.cosyn.util.CodeUtil

sealed trait QA {
    def processInput(context: Context, hole: HoleExpr, input: String): Either[QA, Seq[HoleExpr]]
}

case class ChoiceQA(question: String, choices: Seq[Choice]) extends QA {
    override def toString: String = {
        val choiceString = choices.zipWithIndex.map(p => s"#${p._2 + 1}. ${p._1}").mkString("\n")
        s"$question\n$choiceString"
    }

    override def processInput(context: Context, hole: HoleExpr, input: String): Either[QA, Seq[HoleExpr]] = {
        val pattern = """#(\d)+""".r
        pattern.findFirstMatchIn(input) match {
            case None => ???
            case Some(m) =>
                val newHoles = choices(m.group(1).toInt - 1).action(context, hole)
                Right(newHoles)
        }
    }
}

case class EnumConstantQA(ty: String) extends QA {
    override def toString: String = {
        val simpleName = CodeUtil.qualifiedClassName2Simple(ty).toLowerCase
        s"Which $simpleName?"
    }

    override def processInput(context: Context, hole: HoleExpr, input: String): Either[QA, Seq[HoleExpr]] = {
        val constants = KnowledgeGraph.enumConstants(ty)
        constants.find(_.toLowerCase() == input.toLowerCase()) match {
            case Some(c) =>
                hole.fill = Some(NameExpr(c))
                Right(Seq())
            case None =>
                ???
        }
    }
}