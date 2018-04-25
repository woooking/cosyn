package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{MarkerAnnotationExpr => JPMarkerAnnotationExpr}

class MarkerAnnotationExpr(override val delegate: JPMarkerAnnotationExpr) extends Expression[JPMarkerAnnotationExpr] {
    val name: String = delegate.getName.asString()
}

object MarkerAnnotationExpr {
    def apply(delegate: JPMarkerAnnotationExpr): MarkerAnnotationExpr = new MarkerAnnotationExpr(delegate)

    def unapply(arg: MarkerAnnotationExpr): Option[String] = Some(arg.name)
}