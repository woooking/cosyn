package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.body.{AnnotationDeclaration => JPAnnotationDeclaration}

import scala.collection.JavaConverters._

class AnnotationDeclaration(override val delegate: JPAnnotationDeclaration) extends TypeDeclaration[JPAnnotationDeclaration] {

    val name: String = delegate.getNameAsString

    val members: List[BodyDeclaration[_]] = delegate.getMembers.asScala.map(t => BodyDeclaration(t)).toList
}

object AnnotationDeclaration {
    def apply(delegate: JPAnnotationDeclaration): AnnotationDeclaration = new AnnotationDeclaration(delegate)

    def unapply(arg: AnnotationDeclaration): Option[(
        String,
            List[BodyDeclaration[_]]
        )] = Some((
        arg.name,
        arg.members
    ))
}