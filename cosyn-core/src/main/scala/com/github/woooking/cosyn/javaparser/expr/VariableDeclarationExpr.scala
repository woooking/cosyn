package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{VariableDeclarationExpr => JPVariableDeclarationExpr}
import com.github.woooking.cosyn.javaparser.body.VariableDeclarator

import scala.collection.JavaConverters._

class VariableDeclarationExpr(override val delegate: JPVariableDeclarationExpr) extends Expression[JPVariableDeclarationExpr] {
    val variables: List[VariableDeclarator] = delegate.getVariables.asScala.map(VariableDeclarator.apply).toList
}

object VariableDeclarationExpr {
    def apply(delegate: JPVariableDeclarationExpr): VariableDeclarationExpr = new VariableDeclarationExpr(delegate)

    def unapply(arg: VariableDeclarationExpr): Option[List[VariableDeclarator]] = Some(arg.variables)
}