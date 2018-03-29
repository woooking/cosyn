package com.github.woooking.cosyn.javaparser.modules

import com.github.javaparser.ast.modules.{ModuleDeclaration => JPModuleDeclaration}
import com.github.woooking.cosyn.javaparser.body.TypeDeclaration
import com.github.woooking.cosyn.javaparser.{CompilationUnit, NodeDelegate, PackageDeclaration}

// TODO
class ModuleDeclaration(override val delegate: JPModuleDeclaration) extends NodeDelegate[JPModuleDeclaration] {
}

object ModuleDeclaration {
    def apply(delegate: JPModuleDeclaration): ModuleDeclaration = new ModuleDeclaration(delegate)

    def unapply(arg: CompilationUnit): Option[(
        Option[PackageDeclaration],
            List[ModuleDeclaration],
            Option[ModuleDeclaration],
            List[TypeDeclaration[_]]
        )] = ???
}