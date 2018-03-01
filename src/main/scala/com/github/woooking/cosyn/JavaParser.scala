package com.github.woooking.cosyn

import better.files.File
import com.github.javaparser.{JavaParser => JP}
import com.github.woooking.cosyn.javaparser.CompilationUnit

object JavaParser {

    def parseFile(file: File): CompilationUnit = {
        CompilationUnit(JP.parse(file.toJava), file.name)
    }
}
