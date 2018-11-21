package com.github.woooking.cosyn.pattern.model.expr

import akka.japi.Option.Some

case class MethodCallExpr(receiver: Option[Expression], receiverType: String, methodSignature: String, args: Seq[Expression]) extends Expression

object MethodCallExpr {
    def apply(receiver: Expression, receiverType: String, methodSignature: String, args: Expression*): MethodCallExpr = {
        val methodCallExpr = new MethodCallExpr(Some(receiver), receiverType, methodSignature, args)
        receiver.parent = methodCallExpr
        args.foreach(_.parent = methodCallExpr)
        methodCallExpr
    }
}

