package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{ArrayCreationExpr => JPArrayCreationExpr}

import scala.collection.JavaConverters._
import com.github.woooking.cosyn.util.OptionConverters._

class ArrayCreationExpr(override val delegate: JPArrayCreationExpr) extends Expression[JPArrayCreationExpr] {
    val ty: Type = delegate.getElementType
    val levels: List[ArrayCreationLevel] = delegate.getLevels.asScala.map(ArrayCreationLevel.apply).toList
    val initializer: Option[ArrayInitializerExpr] = delegate.getInitializer.asScala.map(ArrayInitializerExpr.apply)
}

object ArrayCreationExpr {
    def apply(delegate: JPArrayCreationExpr): ArrayCreationExpr = new ArrayCreationExpr(delegate)

    def unapply(arg: ArrayCreationExpr): Option[(
        Type,
            List[ArrayCreationLevel],
            Option[ArrayInitializerExpr]
        )] = Some((
        arg.ty,
        arg.levels,
        arg.initializer
    ))
}


