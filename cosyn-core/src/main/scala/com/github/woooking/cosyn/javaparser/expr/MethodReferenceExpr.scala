package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{MethodReferenceExpr => JPMethodReferenceExpr}
import com.github.woooking.cosyn.util.OptionConverters._
import scala.collection.JavaConverters._
import cats.instances.option._
import cats.instances.list._

class MethodReferenceExpr(override val delegate: JPMethodReferenceExpr) extends Expression[JPMethodReferenceExpr] {
    val scope: Expression[_] = delegate.getScope
    val typeArgs: Option[List[Type]] = delegate.getTypeArguments.asScala
    val identifier: String = delegate.getIdentifier
}

object MethodReferenceExpr {
    def apply(delegate: JPMethodReferenceExpr): MethodReferenceExpr = new MethodReferenceExpr(delegate)

    def unapply(arg: MethodReferenceExpr): Option[(
        Expression[_],
            Option[List[Type]],
            String,
        )] = Some((
        arg.scope,
        arg.typeArgs,
        arg.identifier
    ))
}