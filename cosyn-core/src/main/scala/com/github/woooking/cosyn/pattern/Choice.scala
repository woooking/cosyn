package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallArgs, MethodCallExpr, NameExpr}
import com.github.woooking.cosyn.util.CodeUtil

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
        hole.fill = Some(NameExpr(name))
        Resolved(Seq())
    }
}

case class ConstructorChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"A new ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): ChoiceResult= UnImplemented
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
        hole.fill = Some(MethodCallExpr(NameExpr(CodeUtil.qualifiedClassName2Simple(ty)), ty, method.getSimpleName, args: _*))
        Resolved(args.map(_.value.asInstanceOf[HoleExpr]))
    }
}

case class GetChoice(ty: String, method: MethodEntity) extends Choice {
    override def toString: String = s"From a ${CodeUtil.qualifiedClassName2Simple(ty)}(${method.getQualifiedSignature})"

    override def action(context: Context, hole: HoleExpr): ChoiceResult = {
        val receiver = HoleExpr()
        val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
        hole.fill = Some(MethodCallExpr(receiver, ty, method.getSimpleName, args: _*))
        Resolved(args.map(_.value.asInstanceOf[HoleExpr]) :+ receiver)
    }
}