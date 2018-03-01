package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.`type`.TypeParameter
import com.github.javaparser.ast.body.{EnumConstantDeclaration, EnumDeclaration => JPEnumDeclaration}

import scala.collection.JavaConverters._

class EnumDeclaration(override val delegate: JPEnumDeclaration) extends TypeDeclaration[JPEnumDeclaration] {

    val name: String = delegate.getNameAsString

    val implementedTypes: List[String] = delegate.getImplementedTypes.asScala.map(_.asString()).toList

    val entries: List[EnumConstantDeclaration] = delegate.getEntries.asScala.toList

    val members: List[BodyDeclaration[_]] = delegate.getMembers.asScala.map(t => BodyDeclaration(t)).toList
}

object EnumDeclaration {
    def apply(delegate: JPEnumDeclaration): EnumDeclaration = new EnumDeclaration(delegate)

    def unapply(arg: EnumDeclaration): Option[(
        String,
            List[String],
            List[EnumConstantDeclaration],
            List[BodyDeclaration[_]]
        )] = Some((
        arg.name,
        arg.implementedTypes,
        arg.entries,
        arg.members
    ))
}