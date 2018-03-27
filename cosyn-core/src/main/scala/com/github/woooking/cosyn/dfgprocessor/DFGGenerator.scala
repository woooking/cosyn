package com.github.woooking.cosyn.dfgprocessor

import better.files.File
import com.github.javaparser.ParseProblemException
import com.github.woooking.cosyn.{GraphGenerator, JavaParser}
import com.github.woooking.cosyn.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.dfgprocessor.dfg.DFG
import com.github.woooking.cosyn.filter.{CompilationUnitFilter, DFGFilter}
import com.github.woooking.cosyn.dfgprocessor.ir.Visitor
import com.github.woooking.cosyn.javaparser.CompilationUnit

import scala.collection.mutable

case class DFGGenerator() extends GraphGenerator[File, DFG] {
    val compilationUnitFilters = mutable.ArrayBuffer[CompilationUnitFilter]()
    val dfgFilters = mutable.ArrayBuffer[DFGFilter]()

    def register(filter: CompilationUnitFilter): Unit = {
        compilationUnitFilters += filter
    }

    def register(filter: DFGFilter): Unit = {
        dfgFilters += filter
    }

    private def pipeline(sourceFile: File): Seq[DFG] = {
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

    private def pipeline(compilationUnit: CompilationUnit): Seq[DFG] = {
        if ((true /: compilationUnitFilters) ((valid, f) => valid && f.valid(compilationUnit))) pipeline(Visitor.generateCFGs(compilationUnit))
        else Seq.empty
    }

    private def pipeline(cfgs: Seq[CFG]): Seq[DFG] = {
        cfgs.map(DFG.apply)
    }


    override def generate(data: Seq[File]): Seq[DFG] = data.flatMap(pipeline)
}
