package com.github.woooking.cosyn

import better.files.File.home
import com.github.woooking.cosyn.ir.Visitor

object Main {
    def main(args: Array[String]): Unit = {
        val parser = new ProjectParser(home / "java" / "SnowGraph" / "src" / "main" / "java")
        val cu = parser.parseFile(home / "java" / "SnowGraph" / "src" / "main" / "java" / "extractors" / "linkers" / "codeindoc" / "CodeIndexes.java")
        val visitor = new Visitor(parser)
        visitor.generateCFGs(cu)
        ()
    }
}
