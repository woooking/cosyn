package com.github.woooking.cosyn.dfgprocessor

import better.files.File
import com.github.javaparser.ParseProblemException
import com.github.woooking.cosyn.JavaParser
import com.github.woooking.cosyn.api.GraphGenerator
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.filter.{DFGFilter, NodeFilter}
import com.github.woooking.cosyn.javaparser.CompilationUnit

import scala.collection.mutable

case class DFGGenerator(maxNode: Option[Int]) extends GraphGenerator[File, SimpleDFG] {
    private[this] val nodeFilters = mutable.ArrayBuffer[NodeFilter]()
    private[this] val dfgFilters = mutable.ArrayBuffer[DFGFilter]()

    def register(filter: NodeFilter): Unit = {
        nodeFilters += filter
    }

    def register(filter: DFGFilter): Unit = {
        dfgFilters += filter
    }

    private def pipeline(sourceFile: File): Seq[SimpleDFG] = {
        try {
            pipeline(JavaParser.parseFile(sourceFile))
        } catch {
            case e: NumberFormatException =>
                println(e.getMessage)
                Seq.empty
            case e: ParseProblemException =>
                println(e.getMessage)
                Seq.empty
        }
    }

    private def pipeline(compilationUnit: CompilationUnit): Seq[SimpleDFG] = {
//        if ((true /: nodeFilters) ((valid, f) => valid && f.valid(compilationUnit.delegate))) pipeline(new SimpleVisitor().generateCFGs(compilationUnit))
//        else Seq.empty
        Seq.empty
    }

    private def pipeline(cfgs: Seq[CFGImpl]): Seq[SimpleDFG] = {
        cfgs.map(SimpleDFG.apply)
    }


    override def generate(data: Seq[File]): Seq[SimpleDFG] = {
        val dfgs = data.flatMap(pipeline)
        maxNode match {
            case None => dfgs
            case Some(n) => dfgs.filter(_.getNodeCount < n)
        }
    }
}
