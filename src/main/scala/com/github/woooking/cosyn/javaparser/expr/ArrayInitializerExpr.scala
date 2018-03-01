package com.github.woooking.cosyn.javaparser.expr

import cats.instances.list._
import com.github.javaparser.ast.expr.{ArrayInitializerExpr => JPArrayInitializerExpr}

import scala.collection.JavaConverters._

class ArrayInitializerExpr(override val delegate: JPArrayInitializerExpr) extends Expression[JPArrayInitializerExpr] {
    val values: List[Expression[_]] = delegate.getValues.asScala.toList
}

object ArrayInitializerExpr {
    def apply(delegate: JPArrayInitializerExpr): ArrayInitializerExpr = new ArrayInitializerExpr(delegate)

    def unapply(arg: ArrayInitializerExpr): Option[List[Expression[_]]] = Some(arg.values)
}


