package com.github.woooking.cosyn

import better.files.File.home
import com.github.woooking.cosyn.ir.Visitor

object Main {
    def main(args: Array[String]): Unit = {
        val parser = new ProjectParser(home / "java" / "SnowGraph" / "src" / "main" / "java")
        val file = home / "java" / "SnowGraph" / "src" / "main" / "java" / "extractors" / "linkers" / "codeindoc" / "CodeIndexes.java"
        val cu = parser.parseFile(file)
        val visitor = new Visitor(parser)
        println(file.contentAsString)
        println("==========")
        val methods = visitor.generateCFGs(cu)
        methods.foreach {
            case (name, cfg) =>
                println("-----")
                println(name)
                cfg.print()
        }
        ()
    }
}
