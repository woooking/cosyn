package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.hole_resolver.QAHelper
import com.github.woooking.cosyn.entity.{MethodEntity, TypeEntity}
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph}
import com.github.woooking.cosyn.skeleton.model._
import com.github.woooking.cosyn.util.CodeUtil
import com.github.woooking.cosyn.skeleton.model.BasicType
import CodeBuilder._
import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.visitors.FillHoleVisitor

sealed trait ChoiceResult

case class NewQA(qa: Question) extends ChoiceResult

case class Resolved(newContext: Context, newPattern: Pattern) extends ChoiceResult

case object UnImplemented extends ChoiceResult

sealed trait Choice {
    def action(context: Context, pattern: Pattern, hole: HoleExpr): ChoiceResult
}

case class RecommendChoice(newContext: Context, newPattern: Pattern, score: Double) extends Choice {
    override def toString: String = s"${newPattern.stmts.generateCode("")} ($score)"

    override def action(context: Context, pattern: Pattern, hole: HoleExpr): ChoiceResult = Resolved(newContext, newPattern)
}

case class VariableChoice(name: String) extends Choice {
    override def toString: String = name

    override def action(context: Context, pattern: Pattern, hole: HoleExpr): ChoiceResult = {
        Resolved(context, pattern.fillHole(hole, name))
    }
}

case class MethodCategoryChoice(ty: BasicType, category: QAHelper.MethodCategory, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = category.questionGenerator(ty.ty)

    override def action(context: Context, pattern: Pattern, hole: HoleExpr): ChoiceResult =
        NewQA(ChoiceQuestion("Which method?", methods.map(MethodChoice.apply).toSeq))
}

case class MethodChoice(method: MethodEntity) extends Choice {
    override def toString: String = {
        val javadoc = Option(method.getJavadoc)
        val sentence = javadoc.map(_.getDescription)
            .map(JavadocUtil.extractFirstSentence)
            .getOrElse(method.getQualifiedSignature)
        s"""
           |${method.getQualifiedSignature}
           |$sentence
        """.stripMargin
    }

    override def action(context: Context, pattern: Pattern, hole: HoleExpr): ChoiceResult = method match {
        case _ if method.isConstructor =>
            UnImplemented
        case _ if method.isStatic =>
            val receiverType = method.getDeclareType.getQualifiedName
            val args = CodeUtil.methodParams(method.getSignature).map(ty => arg(ty, HoleExpr()))
            val newPattern = pattern.fillHole(hole, call(CodeUtil.qualifiedClassName2Simple(receiverType), receiverType, method.getSimpleName, args: _*))
            Resolved(context, newPattern)
        case _ =>
            val receiverType = method.getDeclareType.getQualifiedName
            val receiver = HoleExpr()
            val args = CodeUtil.methodParams(method.getSignature).map(ty => arg(ty, HoleExpr()))
            val newPattern = pattern.fillHole(hole, call(receiver, receiverType, method.getSimpleName, args: _*))
            Resolved(context, newPattern)
    }
}

case class IterableChoice(path: List[TypeEntity], recommendVar: Option[String]) extends Choice {
    override def toString: String = recommendVar match {
        case None =>
            val requireObject = path.last.getSimpleName.toLowerCase()
            if (path.size == 1) s"A $requireObject" else s"Some ${requireObject}s in a ${path.head.getSimpleName.toLowerCase()}"
        case Some(name) => name
    }

    private def buildForEachStmt(context: Context, pattern: Pattern, hole: HoleExpr, outer: TypeEntity, inner: TypeEntity, remains: List[TypeEntity]): (ForEachStmt, String) = {
        remains match {
            case Nil =>
                val iterableName = context.findFreeVariableName(BasicType(outer.getQualifiedName))
                val varName = context.findFreeVariableName(BasicType(inner.getQualifiedName))
                val forEachStmt = foreach(inner.getQualifiedName, varName, iterableName, block(pattern.parentStmtOf(hole)))
                (FillHoleVisitor.fillHole(forEachStmt, hole, varName), iterableName)
            case head :: tail =>
                val (innerForEach, innerName) = buildForEachStmt(context, pattern, hole, inner, head, tail)
                val iterableName = context.findFreeVariableName(BasicType(outer.getQualifiedName))
                val forEachStmt = ForEachStmt(inner.getQualifiedName, innerName, iterableName, block(innerForEach))
                (forEachStmt, iterableName)
        }
    }

    override def action(context: Context, pattern: Pattern, hole: HoleExpr): ChoiceResult = {
        val targetType = BasicType(path.head.getQualifiedName)
        if (path.size == 1) {
            recommendVar match {
                case Some(name) =>
                    Resolved(context, pattern.fillHole(hole, name))
                case None =>
                    NewQA(QAHelper.choiceQAForType(context, targetType))
            }
        } else {
            val stmt = pattern.parentStmtOf(hole)
            val blockStmt = pattern.parentOf(stmt).asInstanceOf[BlockStmt]
            val init = HoleExpr()
            val (innerForEach, innerName) = buildForEachStmt(context, pattern, hole, path.head, path.tail.head, path.tail.tail)
            val varDecl = recommendVar match {
                case Some(name) =>
                    v(targetType, innerName, name)
                case None =>
                    v(targetType, innerName, init)
            }
            Resolved(context, pattern.replaceStmtInBlock(blockStmt, stmt, varDecl, innerForEach))
        }
    }
}

object IterableChoice {
    def apply(path: List[TypeEntity], recommendVar: String): IterableChoice = new IterableChoice(path, Some(recommendVar))

    def apply(path: List[TypeEntity]): IterableChoice = new IterableChoice(path, None)
}