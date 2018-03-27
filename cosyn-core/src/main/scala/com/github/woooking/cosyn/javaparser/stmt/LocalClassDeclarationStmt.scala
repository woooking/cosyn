package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{LocalClassDeclarationStmt => JPLocalClassDeclarationStmt}
import com.github.woooking.cosyn.javaparser.body.ClassOrInterfaceDeclaration

class LocalClassDeclarationStmt(override val delegate: JPLocalClassDeclarationStmt) extends Statement {
    val decl: ClassOrInterfaceDeclaration = ClassOrInterfaceDeclaration(delegate.getClassDeclaration)
}

object LocalClassDeclarationStmt {
    def apply(delegate: JPLocalClassDeclarationStmt): LocalClassDeclarationStmt = new LocalClassDeclarationStmt(delegate)

    def unapply(arg: LocalClassDeclarationStmt): Option[ClassOrInterfaceDeclaration] = Some(arg.decl)
}