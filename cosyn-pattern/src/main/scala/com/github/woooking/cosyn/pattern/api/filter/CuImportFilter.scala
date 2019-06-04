package com.github.woooking.cosyn.pattern.api.filter

import com.github.javaparser.ast.CompilationUnit
import com.github.woooking.cosyn.pattern.javaimpl.dfg.{DFGNode, SimpleDFG}

case class CuImportFilter(content: String) extends SeqFilter[CompilationUnit] {
    override def valid(input: CompilationUnit): Boolean = {
        input.getImports.stream()
            .anyMatch(_.getName.asString().startsWith(content))
    }
}
