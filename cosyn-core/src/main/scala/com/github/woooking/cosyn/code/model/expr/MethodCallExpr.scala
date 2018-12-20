package com.github.woooking.cosyn.code.model.expr

import akka.japi.Option.Some
import com.github.woooking.cosyn.code.model.Node
import com.github.woooking.cosyn.code.model.ty.{BasicType, Type}

case class MethodCallArgs(ty: Type, value: Expression) {
    override def toString: String = value.toString
}

object MethodCallArgs {
    def apply(ty: String, value: Expression): MethodCallArgs = new MethodCallArgs(BasicType(ty), value)
}

case class MethodCallExpr private (receiver: Option[Expression], receiverType: BasicType, simpleName: String, args: Seq[MethodCallArgs]) extends Expression {
    receiver.foreach(_.parent = this)
    args.foreach(_.value.parent = this)

    override def toString: String = s"${receiver.map(r => s"$r.").getOrElse("")}$simpleName(${args.mkString(", ")})"

    def getQualifiedSignature = s"$receiverType.$simpleName(${args.map(_.ty).mkString(", ")})"

    override def children: Seq[Node] = receiver.toSeq ++ args.map(_.value)
}

object MethodCallExpr {
    def apply(receiver: Expression, receiverType: BasicType, simpleName: String, args: MethodCallArgs*): MethodCallExpr =
        new MethodCallExpr(Some(receiver), receiverType, simpleName, args)

    def apply(receiver: Expression, receiverType: String, simpleName: String, args: MethodCallArgs*): MethodCallExpr =
        new MethodCallExpr(Some(receiver), BasicType(receiverType), simpleName, args)
}

