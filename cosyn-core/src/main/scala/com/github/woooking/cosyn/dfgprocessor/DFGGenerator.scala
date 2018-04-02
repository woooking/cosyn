package com.github.woooking.cosyn.dfgprocessor

import better.files.File
import com.github.javaparser.ParseProblemException
import com.github.woooking.cosyn.{GraphGenerator, JavaParser}
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.filter.{CompilationUnitFilter, DFGFilter}
import com.github.woooking.cosyn.javaparser.CompilationUnit

import scala.collection.mutable

case class DFGGenerator() extends GraphGenerator[File, SimpleDFG] {
    private[this] val compilationUnitFilters = mutable.ArrayBuffer[CompilationUnitFilter]()
    private[this] val dfgFilters = mutable.ArrayBuffer[DFGFilter]()

    def register(filter: CompilationUnitFilter): Unit = {
        compilationUnitFilters += filter
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
        if ((true /: compilationUnitFilters) ((valid, f) => valid && f.valid(compilationUnit))) pipeline(new SimpleVisitor().generateCFGs(compilationUnit))
        else Seq.empty
    }

    private def pipeline(cfgs: Seq[CFGImpl]): Seq[SimpleDFG] = {
        cfgs.map(SimpleDFG.apply)
    }


    override def generate(data: Seq[File]): Seq[SimpleDFG] = data.flatMap(pipeline)
}
