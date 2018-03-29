package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.body.{
    TypeDeclaration => JPTypeDeclaration,
    AnnotationDeclaration => JPAnnotationDeclaration,
    ClassOrInterfaceDeclaration => JPClassOrInterfaceDeclaration,
    EnumDeclaration => JPEnumDeclaration,
}

trait TypeDeclaration[T <: JPTypeDeclaration[_]] extends BodyDeclaration[T] {
}

object TypeDeclaration {
    def apply[T <: JPTypeDeclaration[_]](typeDecl: JPTypeDeclaration[T]) = typeDecl match {
        case t: JPAnnotationDeclaration => AnnotationDeclaration(t)
        case t: JPClassOrInterfaceDeclaration => ClassOrInterfaceDeclaration(t)
        case t: JPEnumDeclaration => EnumDeclaration(t)
    }
}

