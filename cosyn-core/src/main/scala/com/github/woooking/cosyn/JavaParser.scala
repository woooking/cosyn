package com.github.woooking.cosyn

import better.files.File
import com.github.javaparser.{ParserConfiguration, JavaParser => JP}
import com.github.woooking.cosyn.javaparser.CompilationUnit

object JavaParser {

    def parseFile(file: File, config: ParserConfiguration = null): CompilationUnit = {
        if (config != null) JP.setStaticConfiguration(config)
        CompilationUnit(JP.parse(file.toJava), file.pathAsString)
    }
}
