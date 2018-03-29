package com.github.woooking.cosyn.javaparser.body

import cats.instances.option._
import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.body.{AnnotationMemberDeclaration => JPAnnotationMemberDeclaration}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

class AnnotationMemberDeclaration(override val delegate: JPAnnotationMemberDeclaration) extends BodyDeclaration[JPAnnotationMemberDeclaration] {
    val name: String = delegate.getNameAsString

    val ty: Type = delegate.getType

    val defaultValue: Option[Expression[_]] = delegate.getDefaultValue.asScala

}

object AnnotationMemberDeclaration {
    def apply(delegate: JPAnnotationMemberDeclaration): AnnotationMemberDeclaration = new AnnotationMemberDeclaration(delegate)

    def unapply(arg: AnnotationMemberDeclaration): Option[(
        String,
            Type,
            Option[Expression[_]]
        )] = Some((
        arg.name,
        arg.ty,
        arg.defaultValue
    ))
}