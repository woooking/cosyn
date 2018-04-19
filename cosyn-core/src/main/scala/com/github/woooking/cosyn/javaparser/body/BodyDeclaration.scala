package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.{AnnotationMemberDeclaration => JPAnnotationMemberDeclaration, BodyDeclaration => JPBodyDeclaration, ConstructorDeclaration => JPConstructorDeclaration, FieldDeclaration => JPFieldDeclaration, InitializerDeclaration => JPInitializerDeclaration, MethodDeclaration => JPMethodDeclaration, TypeDeclaration => JPTypeDeclaration}
import com.github.woooking.cosyn.javaparser.NodeDelegate

trait BodyDeclaration[T <: JPBodyDeclaration[_]] extends NodeDelegate[JPBodyDeclaration[T]]

object BodyDeclaration {
    def apply[T <: JPBodyDeclaration[_]](typeDecl: JPBodyDeclaration[T]): BodyDeclaration[_<: Node] = typeDecl match {
        case t: JPAnnotationMemberDeclaration => AnnotationMemberDeclaration(t)
        case t: JPConstructorDeclaration => ConstructorDeclaration(t)
        case t: JPFieldDeclaration => FieldDeclaration(t)
        case t: JPInitializerDeclaration => InitializerDeclaration(t)
        case t: JPMethodDeclaration => MethodDeclaration(t)
        case t: JPTypeDeclaration[_] => TypeDeclaration(t)
    }
}



