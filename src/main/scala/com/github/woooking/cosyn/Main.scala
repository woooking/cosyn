package com.github.woooking.cosyn

import better.files.File.home
import com.github.woooking.cosyn.dfg.DFG
import com.github.woooking.cosyn.ir.Visitor
import com.github.woooking.cosyn.ui.CFGViewer

object Main {
    def main(args: Array[String]): Unit = {
        val parser = new ProjectParser(home / "java" / "SnowGraph" / "src" / "main" / "java")
        val file = home / "java" / "SnowGraph" / "src" / "main" / "java" / "extractors" / "miners" / "mailcode" / "CodeMerge.java"
        val cu = parser.parseFile(file)
        val visitor = new Visitor(parser)
        val methods = visitor.generateCFGs(cu)
        new CFGViewer(file.contentAsString, methods).main(args)
//        methods.foreach {
//            case (name, cfg) =>
//                println("========")
//                println(name)
//                DFG(cfg)
//        }
        ()
    }
}
