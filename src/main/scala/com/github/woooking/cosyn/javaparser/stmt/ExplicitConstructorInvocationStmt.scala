package com.github.woooking.cosyn.javaparser.stmt

import cats.instances.list._
import cats.instances.option._
import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.stmt.{ExplicitConstructorInvocationStmt => JPExplicitConstructorInvocationStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._

class ExplicitConstructorInvocationStmt(override val delegate: JPExplicitConstructorInvocationStmt) extends Statement {
    val isThis: Boolean = delegate.isThis
    val expression: Option[Expression[_]] = delegate.getExpression.asScala
    val typeArgs: Option[List[Type]] = delegate.getTypeArguments.asScala
    val args: List[Expression[_]] = delegate.getArguments.asScala.toList
}

object ExplicitConstructorInvocationStmt {
    def apply(delegate: JPExplicitConstructorInvocationStmt): ExplicitConstructorInvocationStmt = new ExplicitConstructorInvocationStmt(delegate)

    def unapply(arg: ExplicitConstructorInvocationStmt): Option[(
        Boolean,
            Option[Expression[_]],
            Option[List[Type]],
            List[Expression[_]]
        )] = Some((
        arg.isThis,
        arg.expression,
        arg.typeArgs,
        arg.args
    ))
}