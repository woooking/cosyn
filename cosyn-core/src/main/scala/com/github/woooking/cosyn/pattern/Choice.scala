package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.entity.{MethodEntity, TypeEntity}
import com.github.woooking.cosyn.pattern.model.ASTUtil
import com.github.woooking.cosyn.pattern.model.expr._
import com.github.woooking.cosyn.pattern.model.stmt.{BlockStmt, ForEachStmt}
import com.github.woooking.cosyn.util.CodeUtil
import com.github.woooking.cosyn.pattern.model.stmt.ExprStmt._

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

case class ConstructorChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"A new ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = UnImplemented
}

case class StaticChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"Operation in ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        NewQA(ChoiceQA("Which operation?", methods.map(m => StaticMethodChoice(ty, m)).toSeq))
    }
}

case class StaticMethodChoice(ty: String, method: MethodEntity) extends Choice {
    override def toString: String = method.getQualifiedSignature

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
        hole.fill = MethodCallExpr(NameExpr(CodeUtil.qualifiedClassName2Simple(ty)), ty, method.getSimpleName, args: _*)
        Resolved(args.map(_.value.asInstanceOf[HoleExpr]))
    }
}

case class GetChoice(ty: String, method: MethodEntity) extends Choice {
    override def toString: String = s"From a ${CodeUtil.qualifiedClassName2Simple(ty)}(${method.getQualifiedSignature})"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val receiver = HoleExpr()
        val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
        hole.fill = MethodCallExpr(receiver, ty, method.getSimpleName, args: _*)
        Resolved(args.map(_.value.asInstanceOf[HoleExpr]) :+ receiver)
    }
}

case class IterableChoice(path: List[TypeEntity]) extends Choice {
    override def toString: String = path.head.getSimpleName.toLowerCase()

    private def buildForEachStmt(context: Context, hole: HoleExpr, outer: TypeEntity, inner: TypeEntity, remains: List[TypeEntity]): (ForEachStmt, String) = {
        remains match {
            case Nil =>
                val iterableName = outer.getSimpleName.toLowerCase()
                val varName = inner.getSimpleName.toLowerCase()
                val forEachStmt = ForEachStmt(inner.getQualifiedName, varName, NameExpr(iterableName), BlockStmt(ASTUtil.getParentStmt(hole)))
                hole.fill = NameExpr(varName)
                (forEachStmt, iterableName)
            case head :: tail =>
                val (innerForEach, innerName) = buildForEachStmt(context, hole, inner, head, tail)
                val iterableName = outer.getSimpleName.toLowerCase()
                val forEachStmt = ForEachStmt(inner.getQualifiedName, innerName, NameExpr(iterableName), BlockStmt(innerForEach))
                (forEachStmt, iterableName)
        }
    }

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val stmt = ASTUtil.getParentStmt(hole)
        val blockStmt = stmt.parent.asInstanceOf[BlockStmt]
        val init = HoleExpr()
        val (innerForEach, innerName) = buildForEachStmt(context, hole, path.head, path.tail.head, path.tail.tail)
        val varDecl = VariableDeclaration(path.head.getQualifiedName, innerName, init)
        blockStmt.replace(stmt, varDecl, innerForEach)
        Resolved(Seq(init))
    }
}