package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.hole_resolver.QAHelper
import com.github.woooking.cosyn.entity.{MethodEntity, TypeEntity}
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph}
import com.github.woooking.cosyn.code.model.{ASTUtil, CodeBuilder}
import com.github.woooking.cosyn.code.model.expr._
import com.github.woooking.cosyn.code.model.stmt.{BlockStmt, ForEachStmt}
import com.github.woooking.cosyn.util.CodeUtil
import com.github.woooking.cosyn.code.model.ty.BasicType
import CodeBuilder._

sealed trait ChoiceResult

case class NewQA(qa: QA) extends ChoiceResult

case class Resolved(newHoles: Seq[HoleExpr]) extends ChoiceResult

case object UnImplemented extends ChoiceResult

sealed trait Choice {
    def action(context: Context, hole: HoleExpr): ChoiceResult
}

case class VariableChoice(name: String) extends Choice {
    override def toString: String = name

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        hole.fill = NameExpr(name)
        Resolved(Seq())
    }
}

case class MethodCategoryChoice(ty: BasicType, category: QAHelper.MethodCategory, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = category.questionGenerator(ty.ty)

    override def action(context: Context, hole: HoleExpr): ChoiceResult =
        NewQA(ChoiceQA("Which method?", methods.map(MethodChoice.apply).toSeq))
}

case class MethodChoice(method: MethodEntity) extends Choice {
    override def toString: String = {
        val javadoc = JavadocUtil.extractFirstSentence(KnowledgeGraph.getMethodJavadoc(method.getQualifiedSignature).getOrElse(method.getQualifiedSignature))
        s"""
          |${method.getQualifiedSignature}
          |$javadoc
        """.stripMargin
    }

    override def action(context: Context, hole: HoleExpr): ChoiceResult = method match {
        case _ if method.isConstructor =>
            UnImplemented
        case _ if method.isStatic =>
            val receiverType = method.getDeclareType.getQualifiedName
            val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
            hole.fill = MethodCallExpr(NameExpr(CodeUtil.qualifiedClassName2Simple(receiverType)), receiverType, method.getSimpleName, args: _*)
            Resolved(args.map(_.value.asInstanceOf[HoleExpr]))
        case _ =>
            val receiverType = method.getDeclareType.getQualifiedName
            val receiver = HoleExpr()
            println(CodeUtil.methodParams(method.getSignature))
            val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
            hole.fill = MethodCallExpr(receiver, receiverType, method.getSimpleName, args: _*)
            Resolved(args.map(_.value.asInstanceOf[HoleExpr]) :+ receiver)
    }
}

case class IterableChoice(path: List[TypeEntity]) extends Choice {
    override def toString: String = {
        val requireObject = path.last.getSimpleName.toLowerCase()
        if (path.size == 1) s"A $requireObject" else s"Some ${requireObject}s in a ${path.head.getSimpleName.toLowerCase()}"
    }

    private def buildForEachStmt(context: Context, hole: HoleExpr, outer: TypeEntity, inner: TypeEntity, remains: List[TypeEntity]): (ForEachStmt, String) = {
        remains match {
            case Nil =>
                val iterableName = outer.getSimpleName.toLowerCase()
                val varName = inner.getSimpleName.toLowerCase()
                val forEachStmt = ForEachStmt(inner.getQualifiedName, varName, NameExpr(iterableName), block(ASTUtil.getParentStmt(hole)))
                hole.fill = NameExpr(varName)
                (forEachStmt, iterableName)
            case head :: tail =>
                val (innerForEach, innerName) = buildForEachStmt(context, hole, inner, head, tail)
                val iterableName = outer.getSimpleName.toLowerCase()
                val forEachStmt = ForEachStmt(inner.getQualifiedName, innerName, NameExpr(iterableName), block(innerForEach))
                (forEachStmt, iterableName)
        }
    }

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        if (path.size == 1) {
            NewQA(QAHelper.choiceQAForType(context, BasicType(path.head.getQualifiedName)))
        } else {
            val stmt = ASTUtil.getParentStmt(hole)
            val blockStmt = stmt.parent.asInstanceOf[BlockStmt]
            val init = HoleExpr()
            val (innerForEach, innerName) = buildForEachStmt(context, hole, path.head, path.tail.head, path.tail.tail)
            val varDecl = VariableDeclaration(path.head.getQualifiedName, innerName, init)
            blockStmt.replace(stmt, varDecl, innerForEach)
            Resolved(Seq(init))
        }
    }
}