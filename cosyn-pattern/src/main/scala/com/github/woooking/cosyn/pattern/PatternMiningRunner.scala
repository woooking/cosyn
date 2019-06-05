package com.github.woooking.cosyn.pattern

import better.files.File
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.kg.{KnowledgeGraph, PatternSaver}
import com.github.woooking.cosyn.pattern.api.{PatternMiner, Setting}
import com.github.woooking.cosyn.pattern.javaimpl.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.javaimpl.{DFG2Pattern, JavaDFGGenerator}
import de.parsemis.miner.environment.Settings
import org.slf4s.Logging

object PatternMiningRunner extends Logging {
    def main(args: Array[String]): Unit = {
        implicit val setting: Settings[DFGNode, DFGEdge] = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = CosynConfig.global.minFreq)
        val clientCodeRoot = CosynConfig.global.clientCodeDir
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
        result.foreach(p => log.info(p.stmts.generateCode("")))
        PatternSaver.savePatterns(result)
        KnowledgeGraph.close()
    }
}
