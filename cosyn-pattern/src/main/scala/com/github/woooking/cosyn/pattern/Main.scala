package com.github.woooking.cosyn.pattern

import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.pattern.api.Cosyn
import com.github.woooking.cosyn.pattern.dfgprocessor.FromDFGGenerator
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.java.JavaDFGGenerator
import com.github.woooking.cosyn.pattern.mine.Setting
import de.parsemis.miner.environment.Settings

object Main {

    def main(args: Array[String]): Unit = {
        implicit val setting: Settings[DFGNode, DFGEdge] = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = 4, minNodes = 4)
//        val clientCodeRoot = home / "lab" / "client-codes" / "poi"
//        val clientCodeRoot = home / "lab" / "client-codes" / "lucene" / "write-index"
        val clientCodeRoot = home / "lab" / "client-codes" / "test" / "fill-cell-color"
//        val clientCodeRoot = home / "lab" / "client-codes" / "commonmark-java"
        val graphGenerator = JavaDFGGenerator()
        val cosyn = new Cosyn[File, DFGNode, DFGEdge, SimpleDFG, String](
            clientCodeRoot,
            graphGenerator,
            FromDFGGenerator(),
            filterSubGraph = true
        )
        val result = cosyn.process()
        result.foreach(r => {
            println("----- Pattern -----")
            println(r)
        })
    }
}
