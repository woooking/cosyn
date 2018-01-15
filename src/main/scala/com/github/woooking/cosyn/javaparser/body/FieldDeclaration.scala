package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.body.{VariableDeclarator => JPVariableDeclarator, FieldDeclaration => JPFieldDeclaration}

import scala.collection.JavaConverters._

class FieldDeclaration(override val delegate: JPFieldDeclaration) extends BodyDeclaration[JPFieldDeclaration] {
    val variables: List[JPVariableDeclarator] = delegate.getVariables.asScala.toList
}

object FieldDeclaration {
    def apply(delegate: JPFieldDeclaration): FieldDeclaration = new FieldDeclaration(delegate)

    def unapply(arg: FieldDeclaration): Option[List[JPVariableDeclarator]] = Some(arg.variables)
}