package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{ThisExpr => JPThisExpr}
import com.github.woooking.cosyn.util.OptionConverters._
import cats.instances.option._
import com.github.javaparser.ast.Node

class ThisExpr(override val delegate: JPThisExpr) extends Expression[JPThisExpr] {
    val classExpr: Option[Expression[_ <: Node]] = delegate.getClassExpr.asScala
}

object ThisExpr {
    def apply(delegate: JPThisExpr): ThisExpr = new ThisExpr(delegate)

    def unapply(arg: ThisExpr): Option[Option[Expression[_ <: Node]]] = Some(arg.classExpr)
}