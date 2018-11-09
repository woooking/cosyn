package com.github.woooking.cosyn

import java.nio.file.Path

import better.files.File.home
import com.github.woooking.cosyn.api.Cosyn
import com.github.woooking.cosyn.impl.java.JavaDFGGenerator
import com.github.woooking.cosyn.dfgprocessor.FromDFGGenerator
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.mine.Setting

object Main {

    def main(args: Array[String]): Unit = {
        implicit val setting = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = 3, minNodes = 4)

//        val clientCodeRoot = home / "lab" / "client-codes" / "poi"
        val clientCodeRoot = home / "lab" / "client-codes" / "lucene" / "write-index"
//        val clientCodeRoot = home / "lab" / "client-codes" / "commonmark-java"
        val graphGenerator = JavaDFGGenerator()
        val cosyn = new Cosyn[Path, DFGNode, DFGEdge, SimpleDFG, String](
            clientCodeRoot.path,
            graphGenerator,
            FromDFGGenerator()
        )
        val result = cosyn.process()
        result.foreach(r => {
            println("----- Pattern -----")
            println(r)
        })
    }
}
