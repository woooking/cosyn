package com.github.woooking.cosyn.javaparser

import com.github.javaparser.ast.{ImportDeclaration => JPImportDeclaration}
import com.github.woooking.cosyn.javaparser.body.TypeDeclaration
import com.github.woooking.cosyn.javaparser.modules.ModuleDeclaration

// TODO
class ImportDeclaration(override val delegate: JPImportDeclaration) extends NodeDelegate[JPImportDeclaration] {
}

object ImportDeclaration {
    def apply(delegate: JPImportDeclaration): ImportDeclaration = new ImportDeclaration(delegate)

    def unapply(arg: CompilationUnit): Option[(
        Option[PackageDeclaration],
            Seq[ImportDeclaration],
            Option[ModuleDeclaration],
            Seq[TypeDeclaration[_]]
        )] = ???
}