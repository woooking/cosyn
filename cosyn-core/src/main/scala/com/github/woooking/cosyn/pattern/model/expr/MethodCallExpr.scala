package com.github.woooking.cosyn.pattern.model.expr

import akka.japi.Option.Some

case class MethodCallExpr(receiver: Option[Expression], method: String, args: Seq[Expression]) extends Expression

object MethodCallExpr {
    def apply(receiver: Expression, method: String, args: Expression*): MethodCallExpr = new MethodCallExpr(Some(receiver), method, args)
}

