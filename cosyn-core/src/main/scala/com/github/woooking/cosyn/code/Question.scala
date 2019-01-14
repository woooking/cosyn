package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.Question.{ErrorInput, Filled, NewQuestion, Result}
import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.code.model.{BasicType, Type}
import com.github.woooking.cosyn.util.CodeUtil
import CodeBuilder._

sealed trait Question {
    def description: String

    def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Result
}

object Question {

    sealed trait Result

    final case class ErrorInput(message: String) extends Result

    final case class NewQuestion(question: Question) extends Result

    final case class Filled(context: Context, pattern: Pattern) extends Result

}

case class ChoiceQuestion(question: String, choices: Seq[Choice]) extends Question {
    override def description: String = {
        val choiceString = choices.zipWithIndex.map(p => s"#${p._2 + 1}. ${p._1}").mkString("\n")
        s"$question\n$choiceString"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Result = {
        val regex = """#(\d)+""".r
        regex.findFirstMatchIn(input) match {
            case None =>
                ErrorInput("Error Format!")
            case Some(m) =>
                choices(m.group(1).toInt - 1).action(context, pattern, hole) match {
                    case NewQA(qa) => NewQuestion(qa)
                    case Resolved(newContext, newPattern) => Filled(newContext, newPattern)
                    case UnImplemented =>
                        ErrorInput("Not Implemented! Please try other choices.")
                }
        }
    }
}

case class EnumConstantQuestion(ty: BasicType) extends Question {
    override def description: String = {
        val simpleName = CodeUtil.qualifiedClassName2Simple(ty.ty).toLowerCase
        s"Which $simpleName?"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Result = {
        val constants = KnowledgeGraph.enumConstants(ty)
        constants.find(_.toLowerCase() == input.toLowerCase()) match {
            case Some(c) =>
                Filled(context, pattern.fillHole(hole, c))
            case None =>
                ErrorInput(s"Valid inputs are ${constants.map(_.toLowerCase).mkString("/")}.")
        }
    }
}

case class StaticFieldAccessQuestion(receiverType: BasicType, targetType: Type) extends Question {
    override def description: String = {
        s"Which field?"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Result = {
        val fields = KnowledgeGraph.staticFields(receiverType, targetType)
        fields.find(_.toLowerCase() == input.toLowerCase()) match {
            case Some(c) =>
                Filled(context, pattern.fillHole(hole, c))
            case None =>
                ErrorInput(s"Valid inputs are ${fields.map(_.toLowerCase).mkString("/")}.")
        }
    }
}

case class PrimitiveQuestion(hint: Option[String], ty: String) extends Question {
    override def description: String = hint match {
        case Some(h) if ty == "java.lang.String" => s"Please input $h:"
        case Some(h) => s"Please input a $ty($h):"
        case None if ty == "java.lang.String" => "Please input a string:"
        case None => s"Please input a $ty:"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Result = {
        try {
            val expr = ty match {
                case "boolean" => BooleanLiteral(input.toBoolean)
                case "byte" => ByteLiteral(input.toByte)
                case "short" => ShortLiteral(input.toShort)
                case "int" => IntLiteral(input.toInt)
                case "long" => LongLiteral(input.toLong)
                case "float" => FloatLiteral(input.toFloat)
                case "double" => DoubleLiteral(input.toDouble)
                case "char" => CharLiteral(input(0))
                case "java.lang.String" => StringLiteral(input)
            }
            Filled(context, pattern.fillHole(hole, expr))
        } catch {
            case _: NumberFormatException =>
                ErrorInput("Error Format!")
        }
    }
}