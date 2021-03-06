package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.{Node, ArrayCreationLevel => JPArrayCreationLevel}
import com.github.woooking.cosyn.util.OptionConverters._
import cats.instances.option._
import com.github.woooking.cosyn.javaparser.NodeDelegate

class ArrayCreationLevel(override val delegate: JPArrayCreationLevel) extends NodeDelegate[JPArrayCreationLevel] {
    val dimension: Option[Expression[_ <: Node]] = delegate.getDimension.asScala
}

object ArrayCreationLevel {
    def apply(delegate: JPArrayCreationLevel): ArrayCreationLevel = new ArrayCreationLevel(delegate)

    def unapply(arg: ArrayCreationLevel): Option[Option[Expression[_ <: Node]]] = Some(arg.dimension)
}


