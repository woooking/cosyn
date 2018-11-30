package com.github.woooking.cosyn.pattern.model.expr

import akka.japi.Option.Some

case class MethodCallArgs(ty: String, value: Expression) {
    override def toString: String = value.toString
}

case class MethodCallExpr(receiver: Option[Expression], receiverType: String, simpleName: String, args: Seq[MethodCallArgs]) extends Expression {
    override def toString: String = s"${receiver.map(r => s"$r.").getOrElse("")}$simpleName(${args.mkString(", ")})"

    def getQualifiedSignature = s"$receiverType.$simpleName(${args.map(_.ty).mkString(", ")})"
}

object MethodCallExpr {
    def apply(receiver: Expression, receiverType: String, simpleName: String, args: MethodCallArgs*): MethodCallExpr = {
        val methodCallExpr = new MethodCallExpr(Some(receiver), receiverType, simpleName, args)
        receiver.parent = methodCallExpr
        args.foreach(_.value.parent = methodCallExpr)
        methodCallExpr
    }
}

