package com.github.woooking.cosyn.pattern.api.filter

import com.github.javaparser.ast.CompilationUnit

case class CuImportFilter(content: String) extends SeqFilter[CompilationUnit] {
    override def valid(input: CompilationUnit): Boolean = {
        input.getImports.stream()
            .anyMatch(_.getName.asString().startsWith(content))
    }
}
