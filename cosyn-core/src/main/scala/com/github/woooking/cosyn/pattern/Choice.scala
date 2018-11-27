package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallExpr, NameExpr}
import com.github.woooking.cosyn.util.CodeUtil

sealed trait Choice {
    def action(context: Context, hole: HoleExpr): Seq[HoleExpr]
}

case class VariableChoice(name: String) extends Choice {
    override def toString: String = name

    override def action(context: Context, hole: HoleExpr): Seq[HoleExpr] = {
        hole.fill = Some(NameExpr(name))
        Seq()
    }
}

case class ConstructorChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"A new ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): Seq[HoleExpr] = ???
}

case class StaticChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"Operation in ${CodeUtil.qualifiedClassName2Simple(ty)}(${methods.map(_.getSimpleName).mkString("/")})"

    override def action(context: Context, hole: HoleExpr): Seq[HoleExpr] = ???
}

case class GetChoice(ty: String, method: MethodEntity) extends Choice {
    override def toString: String = s"From a ${CodeUtil.qualifiedClassName2Simple(ty)}"

    override def action(context: Context, hole: HoleExpr): Seq[HoleExpr] = {
        val receiver = HoleExpr()
//        val args = method.getSignature
        hole.fill = Some(MethodCallExpr(receiver, ty, method.getSignature))
    }
}