package com.github.woooking.cosyn.pattern

import better.files.Dsl.SymbolicOperations
import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.pattern.api.{PatternMiner, Setting}
import com.github.woooking.cosyn.pattern.dfgprocessor.DFG2Pattern
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.javaimpl.JavaDFGGenerator
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.kg.PatternSaver
import de.parsemis.miner.environment.Settings
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

import scala.annotation.tailrec

object PatternMiningRunner {
    private implicit val formats: Formats = Serialization.formats(NoTypeHints)

    def main(args: Array[String]): Unit = {
        implicit val setting: Settings[DFGNode, DFGEdge] = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = 4, minNodes = 3)
//        val clientCodeRoot = home / "lab" / "client-codes" / "poi"
//        val clientCodeRoot = home / "lab" / "client-codes" / "lucene" / "write-index"
        val clientCodeRoot = home / "lab" / "client-codes" / "test" / "fill-cell-color"
//        val clientCodeRoot = home / "lab" / "client-codes" / "commonmark-java"
//        val clientCodeRoot = CosynConfig.clientCodeDir
        val graphGenerator = JavaDFGGenerator()
//        val cosyn = new Cosyn[File, DFGNode, DFGEdge, SimpleDFG, String](
//            clientCodeRoot,
//            graphGenerator,
//            new FromDFGGenerator(),
//            filterSubGraph = true
//        )
        val cosyn = new PatternMiner[File, DFGNode, DFGEdge, SimpleDFG, Pattern](
            clientCodeRoot,
            graphGenerator,
            new DFG2Pattern(),
            filterSubGraph = true
        )
        val result = cosyn.process()
//        result.foreach(r => {
//            println("----- Pattern -----")
//            println(r)
//        })
        PatternSaver.savePatterns(result)
        KnowledgeGraph.close()
    }
}
