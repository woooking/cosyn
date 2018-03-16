package com.github.woooking.cosyn.filter

import com.github.woooking.cosyn.javaparser.CompilationUnit

trait CompilationUnitFilter {
    def valid(file: CompilationUnit): Boolean
}
