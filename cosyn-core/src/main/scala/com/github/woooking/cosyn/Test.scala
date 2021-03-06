package com.github.woooking.cosyn

import better.files.File.home
import com.github.woooking.cosyn.dfgprocessor.SimpleVisitor
import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG

object Test {

    def main(args: Array[String]): Unit = {
        val file = home / "lab" / "client-codes" / "lucene-solr-master" / "lucene" / "classification" / "src" / "java" / "org" / "apache" / "lucene" / "classification" / "KNearestNeighborClassifier.java"
        val cu = JavaParser.parseFile(file)
        val cfgs = new SimpleVisitor().generateCFGs(cu)
        val cfg = cfgs(5)
        println("======")
    }
}
