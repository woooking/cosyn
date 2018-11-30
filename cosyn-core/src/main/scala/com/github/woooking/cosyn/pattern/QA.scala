package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern.model.expr._
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

case class PrimitiveQA(ty: String) extends QA {
    override def toString: String = if (ty == "java.lang.String") s"Please input a string:"
    else s"Please input a $ty:"

    override def processInput(context: Context, hole: HoleExpr, input: String): Either[QA, Seq[HoleExpr]] = {
        try {
            ty match {
                case "boolean" =>
                    hole.fill = Some(BooleanLiteral(input.toBoolean))
                    Right(Seq())
                case "byte" =>
                    hole.fill = Some(ByteLiteral(input.toByte))
                    Right(Seq())
                case "short" =>
                    hole.fill = Some(ShortLiteral(input.toShort))
                    Right(Seq())
                case "int" =>
                    hole.fill = Some(IntLiteral(input.toInt))
                    Right(Seq())
                case "long" =>
                    hole.fill = Some(LongLiteral(input.toLong))
                    Right(Seq())
                case "float" =>
                    hole.fill = Some(FloatLiteral(input.toFloat))
                    Right(Seq())
                case "double" =>
                    hole.fill = Some(DoubleLiteral(input.toDouble))
                    Right(Seq())
                case "char" =>
                    hole.fill = Some(CharLiteral(input(0)))
                    Right(Seq())
                case "java.lang.String" =>
                    hole.fill = Some(StringLiteral(input))
                    Right(Seq())
            }
        } catch {
            case _: NumberFormatException =>
                Left(this)
        }

    }
}