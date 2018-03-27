package com.github.woooking.cosyn

import better.files.File.home
import com.github.woooking.cosyn.dfgprocessor.dfg.DFG
import com.github.woooking.cosyn.dfgprocessor.ir.Visitor

object Test {

    def main(args: Array[String]): Unit = {
        val file = home / "lab" / "client-codes" / "lucene-solr-master" / "lucene" / "classification" / "src" / "java" / "org" / "apache" / "lucene" / "classification" / "KNearestNeighborClassifier.java"
        val cu = JavaParser.parseFile(file)
        val cfgs = Visitor.generateCFGs(cu)
        val cfg = cfgs(5)
        cfg.print()
        println("======")
        val dfg = DFG(cfg)
        dfg.print()
    }
}
