package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{SuperExpr => JPSuperExpr}
import com.github.woooking.cosyn.util.OptionConverters._
import cats.instances.option._
import com.github.javaparser.ast.Node

class SuperExpr(override val delegate: JPSuperExpr) extends Expression[JPSuperExpr] {
    val classExpr: Option[Expression[_ <: Node]] = delegate.getClassExpr.asScala
}

object SuperExpr {
    def apply(delegate: JPSuperExpr): SuperExpr = new SuperExpr(delegate)

    def unapply(arg: SuperExpr): Option[Option[Expression[_ <: Node]]] = Some(arg.classExpr)
}