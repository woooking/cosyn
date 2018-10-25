package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.`type`.TypeParameter
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration => JPClassOrInterfaceDeclaration}

import scala.collection.JavaConverters._

class ClassOrInterfaceDeclaration(override val delegate: JPClassOrInterfaceDeclaration) extends TypeDeclaration[JPClassOrInterfaceDeclaration] {

    val name: String = delegate.getNameAsString

    val extendedTypes: List[String] = delegate.getExtendedTypes.asScala.map(_.asString()).toList

    val implementedTypes: List[String] = delegate.getImplementedTypes.asScala.map(_.asString()).toList

    val typeParams: List[TypeParameter] = delegate.getTypeParameters.asScala.toList

    val members: List[BodyDeclaration[_]] = delegate.getMembers.asScala.map(t => BodyDeclaration(t)).toList
}

object ClassOrInterfaceDeclaration {
    def apply(delegate: JPClassOrInterfaceDeclaration): ClassOrInterfaceDeclaration = new ClassOrInterfaceDeclaration(delegate)

    def unapply(arg: ClassOrInterfaceDeclaration): Option[(
        String,
            List[String],
            List[String],
            List[TypeParameter],
            List[BodyDeclaration[_]]
        )] = Some((
        arg.name,
        arg.extendedTypes,
        arg.implementedTypes,
        arg.typeParams,
        arg.members
    ))
}