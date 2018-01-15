package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{MethodCallExpr => JPMethodCallExpr}
import com.github.woooking.cosyn.util.OptionConverters._
import scala.collection.JavaConverters._


class MethodCallExpr(override val delegate: JPMethodCallExpr) extends Expression {
    val name: String = delegate.getName.asString()
    val scope: Option[Expression] = delegate.getScope.asScala.map(e => Expression(e))
    val typeArgs: Option[List[Type]] = delegate.getTypeArguments.asScala.map(l => l.asScala.toList)
    val args: List[Expression] = delegate.getScope.asScala.map(e => Expression(e)).toList
}

object MethodCallExpr {
    def apply(delegate: JPMethodCallExpr): MethodCallExpr = new MethodCallExpr(delegate)

    def unapply(arg: MethodCallExpr): Option[(
        String,
            Option[Expression],
            Option[List[Type]],
            List[Expression]
        )] = Some((
        arg.name,
        arg.scope,
        arg.typeArgs,
        arg.args
    ))
}