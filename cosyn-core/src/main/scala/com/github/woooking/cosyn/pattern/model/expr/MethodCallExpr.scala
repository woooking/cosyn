package com.github.woooking.cosyn.pattern.model.expr

import akka.japi.Option.Some
import com.github.woooking.cosyn.pattern.model.Node

case class MethodCallArgs(ty: String, value: Expression) {
    override def toString: String = value.toString
}

case class MethodCallExpr private (receiver: Option[Expression], receiverType: String, simpleName: String, args: Seq[MethodCallArgs]) extends Expression {
    receiver.foreach(_.parent = this)
    args.foreach(_.value.parent = this)

    override def toString: String = s"${receiver.map(r => s"$r.").getOrElse("")}$simpleName(${args.mkString(", ")})"

    def getQualifiedSignature = s"$receiverType.$simpleName(${args.map(_.ty).mkString(", ")})"

    override def children: Seq[Node] = receiver.toSeq ++ args.map(_.value)
}

object MethodCallExpr {
    def apply(receiver: Expression, receiverType: String, simpleName: String, args: MethodCallArgs*): MethodCallExpr =
        new MethodCallExpr(Some(receiver), receiverType, simpleName, args)
}

