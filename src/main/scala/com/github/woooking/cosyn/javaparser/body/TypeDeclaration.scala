package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.body.{
    TypeDeclaration => JPTypeDeclaration,
    ClassOrInterfaceDeclaration => JPClassOrInterfaceDeclaration,
}

trait TypeDeclaration[T <: JPTypeDeclaration[_]] extends BodyDeclaration[T] {
}

object TypeDeclaration {
    def apply[T <: JPTypeDeclaration[_]](typeDecl: JPTypeDeclaration[T]) = typeDecl match {
        case t: JPClassOrInterfaceDeclaration => ClassOrInterfaceDeclaration(t)
        case _ => ???
    }
}

