package com.github.woooking.cosyn

import better.files.File
import com.github.javaparser.{ParserConfiguration, JavaParser => JP}
import com.github.woooking.cosyn.javaparser.CompilationUnit
import com.github.woooking.cosyn.javaparser.stmt.BlockStmt

object JavaParser {
    def parseStatements(statements: String, config: ParserConfiguration = null): BlockStmt = {
        if (config != null) JP.setStaticConfiguration(config)
        BlockStmt(JP.parseBlock(s"{$statements}"))
    }

    def parseFile(file: File, config: ParserConfiguration = null): CompilationUnit = {
        if (config != null) JP.setStaticConfiguration(config)
        CompilationUnit(JP.parse(file.toJava), file.pathAsString)
    }
}
