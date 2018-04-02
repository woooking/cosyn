package com.github.woooking.cosyn.dfgprocessor

import better.files.File
import better.files.File.home
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.{CombinedTypeSolver, JavaParserTypeSolver, ReflectionTypeSolver}
import com.github.woooking.cosyn.dfgprocessor.dfg.QualifiedDFG
import com.github.woooking.cosyn.util.Printable
import com.github.woooking.cosyn.{DataSource, JavaParser}
import org.slf4s.Logging

class CorpusBuilder(projects: Seq[File], dest: File) extends Logging {

    def buildFile(solver: TypeSolver)(file: File): Unit = {
        val cu = JavaParser.parseFile(file)
        val cfgs = new QualifiedVisitor(JavaParserFacade.get(solver)).generateCFGs(cu)
        val dfgs = cfgs.map(QualifiedDFG.apply)
        dfgs.foreach(dfg =>{
            println("=====")
            println(dfg.cfg.name)
            println(dfg.cfg.decl.delegate)
            Printable.print(dfg)
        })
    }

    def buildProject(file: File): Unit = {
        log.info(s"Building graphs from project $file.")
        val solver = new CombinedTypeSolver()
        solver.add(new ReflectionTypeSolver())
        solver.add(new JavaParserTypeSolver(file.toJava))
        val sourceFiles = DataSource.fromJavaSourceCodeDir(file).data.toStream
        sourceFiles.foreach(buildFile(solver))
        log.info(s"Project $file builded.")
    }

    def build(): Unit = {
        projects.foreach(buildProject)
    }
}

object CorpusBuilder {
    def main(args: Array[String]): Unit = {
        val clientCodes = home / "lab" / "java-codes"
        val dest = home / "lab" / "graphs"
        val builder = new CorpusBuilder(clientCodes.list.toSeq, dest)
        builder.build()
    }
}
