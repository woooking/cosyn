package com.github.woooking.cosyn.javaparser

import com.github.javaparser.ast.{CompilationUnit => JPCompilationUnit}
import com.github.woooking.cosyn.javaparser.body.TypeDeclaration
import com.github.woooking.cosyn.javaparser.modules.ModuleDeclaration
import com.github.woooking.cosyn.util.OptionConverters._

import collection.JavaConverters._

class CompilationUnit(override val delegate: JPCompilationUnit, val file: String) extends NodeDelegate[JPCompilationUnit] {
    val packageDeclaration: Option[PackageDeclaration] = delegate.getPackageDeclaration.asScala.map(PackageDeclaration.apply)

    val imports: List[ImportDeclaration] = delegate.getImports.asScala.map(ImportDeclaration.apply).toList

    val module: Option[ModuleDeclaration] = delegate.getModule.asScala.map(ModuleDeclaration.apply)

    val types: List[TypeDeclaration[_]] = delegate.getTypes.asScala.map(t => TypeDeclaration(t)).toList
}

object CompilationUnit {
    def apply(delegate: JPCompilationUnit, file: String): CompilationUnit = new CompilationUnit(delegate, file)

    def unapply(arg: CompilationUnit): Option[(
        Option[PackageDeclaration],
            List[ImportDeclaration],
            Option[ModuleDeclaration],
            List[TypeDeclaration[_]]
        )] = Some((
        arg.packageDeclaration,
        arg.imports,
        arg.module,
        arg.types
    ))
}


