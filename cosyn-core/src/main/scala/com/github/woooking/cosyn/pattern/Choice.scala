package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.pattern.model.expr
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallArgs, MethodCallExpr, NameExpr}
import com.github.woooking.cosyn.util.CodeUtil

sealed trait Choice {
    def action(context: Context, hole: HoleExpr): Either[QA, Seq[HoleExpr]]
}

case class VariableChoice(name: String) extends Choice {
    override def toString: String = name

    override def action(context: Context, hole: HoleExpr): Either[QA, Seq[HoleExpr]] = {
        hole.fill = Some(NameExpr(name))
        Right(Seq())
    }
}

case class ConstructorChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"A new ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): Either[QA, Seq[HoleExpr]]= ???
}

case class StaticChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"Operation in ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): Either[QA, Seq[HoleExpr]] = {
        Left(ChoiceQA("Which operation?", methods.map(m => StaticMethodChoice(ty, m)).toSeq))
    }
}

case class StaticMethodChoice(ty: String, method: MethodEntity) extends Choice {
    override def toString: String = method.getQualifiedSignature

    override def action(context: Context, hole: HoleExpr): Either[QA, Seq[HoleExpr]] = {
        val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
        hole.fill = Some(MethodCallExpr(NameExpr(ty), ty, method.getSimpleName, args: _*))
        Right(args.map(_.value.asInstanceOf[HoleExpr]))
    }
}

case class GetChoice(ty: String, method: MethodEntity) extends Choice {
    override def toString: String = s"From a ${CodeUtil.qualifiedClassName2Simple(ty)}(${method.getQualifiedSignature})"

    override def action(context: Context, hole: HoleExpr): Either[QA, Seq[HoleExpr]] = {
        val receiver = HoleExpr()
        val args = CodeUtil.methodParams(method.getSignature).map(ty => MethodCallArgs(ty, HoleExpr()))
        hole.fill = Some(MethodCallExpr(receiver, ty, method.getSimpleName, args: _*))
        Right(receiver +: args.map(_.value.asInstanceOf[HoleExpr]))
    }
}