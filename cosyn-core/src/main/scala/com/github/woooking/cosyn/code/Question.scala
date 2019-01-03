package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.code.model.ty.{BasicType, Type}
import com.github.woooking.cosyn.util.CodeUtil

sealed trait Question {
    def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Either[Question, (Context, Pattern)]
}

case class ChoiceQuestion(question: String, choices: Seq[Choice]) extends Question {
    override def toString: String = {
        val choiceString = choices.zipWithIndex.map(p => s"#${p._2 + 1}. ${p._1}").mkString("\n")
        s"$question\n$choiceString"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Either[Question, Seq[HoleExpr]] = {
        val pattern = """#(\d)+""".r
        pattern.findFirstMatchIn(input) match {
            case None =>
                println("Error Format!")
                Left(this)
            case Some(m) =>
                choices(m.group(1).toInt - 1).action(context, pattern, hole) match {
                    case NewQA(qa) => Left(qa)
                    case Resolved(newHoles) => Right(newHoles)
                    case UnImplemented =>
                        println("Not Implemented! Please try other choices.")
                        Left(this)
                }
        }
    }
}

case class EnumConstantQuestion(ty: BasicType) extends Question {
    override def toString: String = {
        val simpleName = CodeUtil.qualifiedClassName2Simple(ty.ty).toLowerCase
        s"Which $simpleName?"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Either[Question, Seq[HoleExpr]] = {
        val constants = KnowledgeGraph.enumConstants(ty)
        constants.find(_.toLowerCase() == input.toLowerCase()) match {
            case Some(c) =>
                hole.fill = c
                Right(Seq())
            case None =>
                println(s"Could not understand $input!")
                println(s"Valid inputs are ${constants.map(_.toLowerCase).mkString("/")}.")
                Left(this)
        }
    }
}

case class StaticFieldAccessQuestion(receiverType: BasicType, targetType: Type) extends Question {
    override def toString: String = {
        s"Which field?"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Either[Question, Seq[HoleExpr]] = {
        val fields = KnowledgeGraph.staticFields(receiverType, targetType)
        fields.find(_.toLowerCase() == input.toLowerCase()) match {
            case Some(c) =>
                hole.fill = c
                Right(Seq())
            case None =>
                println(s"Could not understand $input!")
                println(s"Valid inputs are ${fields.map(_.toLowerCase).mkString("/")}.")
                Left(this)
        }
    }
}

case class PrimitiveQuestion(hint: Option[String], ty: String) extends Question {
    override def toString: String = hint match {
        case Some(h) if ty == "java.lang.String" => s"Please input $h:"
        case Some(h) => s"Please input a $ty($h):"
        case None if ty == "java.lang.String" => "Please input a string:"
        case None => s"Please input a $ty:"
    }

    override def processInput(context: Context, pattern: Pattern, hole: HoleExpr, input: String): Either[Question, Seq[HoleExpr]] = {
        try {
            ty match {
                case "boolean" =>
                    hole.fill = BooleanLiteral(input.toBoolean)
                    Right(Seq())
                case "byte" =>
                    hole.fill = ByteLiteral(input.toByte)
                    Right(Seq())
                case "short" =>
                    hole.fill = ShortLiteral(input.toShort)
                    Right(Seq())
                case "int" =>
                    hole.fill = IntLiteral(input.toInt)
                    Right(Seq())
                case "long" =>
                    hole.fill = LongLiteral(input.toLong)
                    Right(Seq())
                case "float" =>
                    hole.fill = FloatLiteral(input.toFloat)
                    Right(Seq())
                case "double" =>
                    hole.fill = DoubleLiteral(input.toDouble)
                    Right(Seq())
                case "char" =>
                    hole.fill = CharLiteral(input(0))
                    Right(Seq())
                case "java.lang.String" =>
                    hole.fill = StringLiteral(input)
                    Right(Seq())
            }
        } catch {
            case _: NumberFormatException =>
                println("Error Format!")
                Left(this)
        }

    }
}