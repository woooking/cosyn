package com.github.woooking.cosyn.core.code

import com.github.woooking.cosyn.core.code.hole_resolver.QAHelper
import com.github.woooking.cosyn.kg.entity.{EnumEntity, MethodEntity, TypeEntity}
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, _}
import com.github.woooking.cosyn.comm.skeleton.visitors.FillHoleVisitor
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.core.nlp.JavadocUtil

sealed trait ChoiceResult

case class NewQA(qa: Question) extends ChoiceResult

case class Resolved(newContext: Context, filled: Expression) extends ChoiceResult

case object UnImplemented extends ChoiceResult

sealed trait Choice {
    def action(context: Context, hole: HoleExpr): ChoiceResult
}

case class RecommendChoice(newContext: Context, filled: Expression, score: Double) extends Choice {
    override def toString: String = s"${newContext.pattern.stmts.generateCode("")} ($score)"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = Resolved(newContext, filled)
}

case class VariableChoice(name: String) extends Choice {
    override def toString: String = name

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        Resolved(context.copy(pattern = context.pattern.fillHole(hole, name)), name)
    }
}

case class CreateArrayChoice(ty: ArrayType) extends Choice {
    override def toString: String = s"Create an array of ${ty.componentType}"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val expr = create(ty.componentType, context.pattern.holeFactory.newHole() :: Nil)
        val newPattern = context.pattern.fillHole(hole, expr)
        Resolved(context.copy(pattern = newPattern), expr)
    }
}

case class MethodCategoryChoice(ty: BasicType, category: QAHelper.MethodCategory, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = category.questionGenerator(ty.ty)

    override def action(context: Context, hole: HoleExpr): ChoiceResult =
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

    override def action(context: Context, hole: HoleExpr): ChoiceResult = method match {
        case _ if method.isConstructor =>
            UnImplemented
        case _ if method.isStatic =>
            val pattern = context.pattern
            val receiverType = method.getDeclareType.getQualifiedName
            val args = CodeUtil.methodParams(method.getSignature).map(ty => arg(ty, pattern.holeFactory.newHole()))
            val expr = call(CodeUtil.qualifiedClassName2Simple(receiverType), BasicType(receiverType), method.getSimpleName, args: _*)
            val newPattern = pattern.fillHole(hole, expr)
            Resolved(context.copy(pattern = newPattern), expr)
        case _ =>
            val pattern = context.pattern
            val receiverType = method.getDeclareType.getQualifiedName
            val receiver = pattern.holeFactory.newHole()
            val args = CodeUtil.methodParams(method.getSignature).map(ty => arg(ty, pattern.holeFactory.newHole()))
            val expr = call(receiver, BasicType(receiverType), method.getSimpleName, args: _*)
            val newPattern = pattern.fillHole(hole, expr)
            Resolved(context.copy(pattern = newPattern), expr)
    }
}

case class EnumChoice(enumEntity: EnumEntity) extends Choice {
    override def toString: String = "Choose one"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val expr = enum(BasicType(enumEntity.getQualifiedName), context.pattern.holeFactory.newHole())
        val newPattern = context.pattern.fillHole(hole, expr)
        Resolved(context.copy(pattern = newPattern), expr)
    }
}

case class IterableChoice(path: List[TypeEntity], recommendVar: Option[String], recommend: Boolean) extends Choice {
    override def toString: String = recommendVar match {
        case None =>
            val requireObject = path.last.getSimpleName.toLowerCase()
            if (path.size == 1) s"A $requireObject" else s"Some ${requireObject}s in a ${path.head.getSimpleName.toLowerCase()}"
        case Some(name) => name
    }

    private def buildForEachStmt(context: Context, hole: HoleExpr, outer: TypeEntity, inner: TypeEntity, remains: List[TypeEntity]): (ForEachStmt, String) = {
        remains match {
            case Nil =>
                val iterableName = context.findFreeVariableName(BasicType(outer.getQualifiedName))
                val varName = context.findFreeVariableName(BasicType(inner.getQualifiedName))
                val forEachStmt = foreach(inner.getQualifiedName, varName, iterableName, block(context.pattern.parentStmtOf(hole)))
                (FillHoleVisitor.fillHole(forEachStmt, hole, varName), iterableName)
            case head :: tail =>
                val (innerForEach, innerName) = buildForEachStmt(context, hole, inner, head, tail)
                val iterableName = context.findFreeVariableName(BasicType(outer.getQualifiedName))
                val forEachStmt = ForEachStmt(inner.getQualifiedName, innerName, iterableName, block(innerForEach))
                (forEachStmt, iterableName)
        }
    }

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val targetType = BasicType(path.head.getQualifiedName)
        if (path.size == 1) {
            recommendVar match {
                case Some(name) =>
                    Resolved(context.copy(pattern = context.pattern.fillHole(hole, name)), name)
                case None =>
                    NewQA(QAHelper.choiceQAForType(context, targetType, recommend))
            }
        } else {
            val pattern = context.pattern
            val stmt = pattern.parentStmtOf(hole)
            val blockStmt = pattern.parentOf(stmt).asInstanceOf[BlockStmt]
            val init = pattern.holeFactory.newHole()
            val (innerForEach, innerName) = buildForEachStmt(context, hole, path.head, path.tail.head, path.tail.tail)
            val varDecl = recommendVar match {
                case Some(name) =>
                    v(targetType, innerName, name)
                case None =>
                    v(targetType, innerName, init)
            }
            Resolved(context.copy(pattern = pattern.replaceStmtInBlock(blockStmt, stmt, varDecl, innerForEach)), varDecl)
        }
    }
}

object IterableChoice {
    def apply(path: List[TypeEntity], recommendVar: String, recommend: Boolean): IterableChoice = new IterableChoice(path, Some(recommendVar), recommend)

    def apply(path: List[TypeEntity], recommend: Boolean): IterableChoice = new IterableChoice(path, None, recommend)
}