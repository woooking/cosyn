package com.github.woooking.cosyn.javaparser

import com.github.javaparser.ast.{PackageDeclaration => JPPackageDeclaration}
import com.github.woooking.cosyn.javaparser.body.TypeDeclaration
import com.github.woooking.cosyn.javaparser.modules.ModuleDeclaration

// TODO
class PackageDeclaration(override val delegate: JPPackageDeclaration) extends NodeDelegate[JPPackageDeclaration] {
}

object PackageDeclaration {
    def apply(delegate: JPPackageDeclaration): PackageDeclaration = new PackageDeclaration(delegate)

    def unapply(arg: CompilationUnit): Option[(
        Option[PackageDeclaration],
            Seq[ImportDeclaration],
            Option[ModuleDeclaration],
            Seq[TypeDeclaration[_]]
        )] = ???
}