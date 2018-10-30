package com.github.woooking.cosyn.actor

import akka.actor.ActorRef
import better.files.Dsl._
import better.files.File
import better.files.File.home
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.{CombinedTypeSolver, JavaParserTypeSolver, ReflectionTypeSolver}
import com.github.woooking.cosyn.CosynJsonProtocol._
import com.github.woooking.cosyn.JavaParser
import com.github.woooking.cosyn.actor.ProjectBuilder.TaskFinished
import com.github.woooking.cosyn.api.DataSource
import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.model.ProjectInfo
import com.github.woooking.cosyn.util.CodeUtil
import spray.json._

import scala.annotation.tailrec

class ProjectProcessor(project: ProjectInfo, builder: ActorRef) extends Runnable {
    val GraphRoot: File = home / "lab" / "graphs"

//    @tailrec
//    private def processFiles(visitor: QualifiedVisitor, files: List[File], num: Int): Int = files match {
//        case Nil => num
//        case f :: fs =>
//            val success = try {
//                val cu = JavaParser.parseFile(f)
//                val pkg = CodeUtil.packageOf(cu)
//                val location = GraphRoot / pkg.split("\\.").mkString("/")
//                val resultFile = location / f.nameWithoutExtension
//                if (!resultFile.exists) {
//                    val dfgs = visitor.generateCFGs(cu).map(SimpleDFG.apply)
//                    mkdirs(location)
//                    dfgs.toJson.prettyPrint >>: resultFile
//                }
//                1
//            } catch {
//                case e: Throwable =>
//                    e.printStackTrace()
//                    println(f.lines.mkString("\n"))
//                    0
//            }
//            processFiles(visitor, fs, num + success)
//    }

    override def run(): Unit = {
//        val solver = new CombinedTypeSolver()
//        solver.add(new ReflectionTypeSolver())
//        project.roots.map(_.toJava).map(f => new JavaParserTypeSolver(f)).foreach(solver.add)
//        val facade = JavaParserFacade.get(solver)
//        val visitor = new QualifiedVisitor(facade)
//        val dataSource = DataSource.fromJavaSourceCodeDir(project.roots)
//
//        val size = processFiles(visitor, dataSource.data.toList, 0)
//
//        println(s"${builder.path.name}: ${project.name} have $size files")
//        builder ! TaskFinished
    }
}

