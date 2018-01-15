package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.body.{
    BodyDeclaration => JPBodyDeclaration,
    TypeDeclaration => JPTypeDeclaration,
    FieldDeclaration => JPFieldDeclaration,
    ConstructorDeclaration => JPConstructorDeclaration,
}
import com.github.woooking.cosyn.javaparser.NodeDelegate

trait BodyDeclaration[T <: JPBodyDeclaration[_]] extends NodeDelegate[JPBodyDeclaration[T]]

object BodyDeclaration {
    def apply[T <: JPBodyDeclaration[_]](typeDecl: JPBodyDeclaration[T]): BodyDeclaration[_] = typeDecl match {
        case t: JPTypeDeclaration[_] => TypeDeclaration(t)
        case t: JPFieldDeclaration => FieldDeclaration(t)
        case t: JPConstructorDeclaration => ConstructorDeclaration(t)
    }
}



