package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{MethodCallExpr => JPMethodCallExpr}
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._
import cats.instances.option._
import cats.instances.list._
import com.github.javaparser.ast.Node

class MethodCallExpr(override val delegate: JPMethodCallExpr) extends Expression[JPMethodCallExpr] {
    val name: String = delegate.getName
    def scope: Option[Expression[_ <: Node]] = delegate.getScope.asScala
    val typeArgs: Option[List[Type]] = delegate.getTypeArguments.asScala
    val args: List[Expression[_ <: Node]] = delegate.getArguments.asScala.toList
}

object MethodCallExpr {
    def apply(delegate: JPMethodCallExpr): MethodCallExpr = new MethodCallExpr(delegate)

    def unapply(arg: MethodCallExpr): Option[(
        String,
            Option[Expression[_ <: Node]],
            Option[List[Type]],
            List[Expression[_ <: Node]]
        )] = Some((
        arg.name,
        arg.scope,
        arg.typeArgs,
        arg.args
    ))
}