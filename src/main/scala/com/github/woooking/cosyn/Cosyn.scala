package com.github.woooking.cosyn

import better.files.File
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.dfg.{DFG, DFGEdge, DFGNode}
import com.github.woooking.cosyn.ir.Visitor
import com.github.woooking.cosyn.javaparser.CompilationUnit
import com.github.woooking.cosyn.middleware.{CompilationUnitFilter, DFGFilter, FileFilter}
import com.github.woooking.cosyn.mine.{Miner, Setting}
import com.github.woooking.cosyn.util.GraphUtil
import de.parsemis.graph.{Edge, Graph, Node}
import de.parsemis.miner.general.Fragment

import scala.collection.mutable
import scala.collection.JavaConverters._

class Cosyn(dir: File) {
    type DNode = Node[DFGNode, DFGEdge]
    type DEdge = Edge[DFGNode, DFGEdge]
    type DGraph = Graph[DFGNode, DFGEdge]
    type DFragment = Fragment[DFGNode, DFGEdge]

    val fileFilters = mutable.ArrayBuffer[FileFilter]()
    val compilationUnitFilters = mutable.ArrayBuffer[CompilationUnitFilter]()
    val dfgFilters = mutable.ArrayBuffer[DFGFilter]()
    var fileCount = 0
    var cfgCount = 0

    private def getJavaFilesFromDirectory(dir: File): List[File] = {
        dir.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toList
    }

    private def pipeline(sourceFile: File): Seq[DFG] = {
        try {
            if ((true /: fileFilters) ((valid, f) => valid && f.valid(sourceFile))) pipeline(JavaParser.parseFile(sourceFile))
            else Seq.empty
        } catch {
            case e: NumberFormatException =>
                println(e.getMessage)
                Seq.empty
        }
    }

    private def pipeline(compilationUnit: CompilationUnit): Seq[DFG] = {
        fileCount += 1
        if ((true /: compilationUnitFilters) ((valid, f) => valid && f.valid(compilationUnit))) pipeline(Visitor.generateCFGs(compilationUnit))
        else Seq.empty
    }

    private def pipeline(cfgs: Seq[CFG]): Seq[DFG] = {
        cfgCount += cfgs.size
        cfgs.map(DFG.apply)
    }

    private def resultFilter(result: Seq[DFragment]): Seq[DGraph] = {
        val graphs: mutable.ArrayBuffer[DGraph] = mutable.ArrayBuffer()
        val edgeSets: mutable.ArrayBuffer[Set[DEdge]] = mutable.ArrayBuffer()
        result.map(_.toGraph).sorted(Ordering.by[Graph[_, _], Int](_.getEdgeCount).reverse).foreach(graph => {
            val ite = graph.edgeIterator()
            val edges = ite.asScala.toSet
            if (!edgeSets.exists(edgeSet => isSubset(edges, edgeSet))) {
                graphs += graph
                edgeSets += edges
            }
        })
        graphs
    }

    private def isSubset(small: Set[DEdge], big: Set[DEdge]): Boolean = small.headOption match {
        case None => true
        case Some(e) =>
            if (big.exists(bigEdge =>
                (bigEdge.getNodeA.getLabel == e.getNodeA.getLabel &&
                    bigEdge.getNodeB.getLabel == e.getNodeB.getLabel &&
                    bigEdge.getDirection == e.getDirection ||
                    bigEdge.getNodeA.getLabel == e.getNodeB.getLabel &&
                        bigEdge.getNodeB.getLabel == e.getNodeA.getLabel &&
                        bigEdge.getDirection == -e.getDirection) &&
                    bigEdge.getLabel == e.getLabel)) isSubset(small.tail, big)
            else false
    }

    def register(filter: FileFilter): Unit = {
        fileFilters += filter
    }

    def register(filter: CompilationUnitFilter): Unit = {
        compilationUnitFilters += filter
    }

    def register(filter: DFGFilter): Unit = {
        dfgFilters += filter
    }

    def process(): Unit = {
        implicit val setting = Setting.create()
        val dfgs = (getJavaFilesFromDirectory(dir).flatMap(pipeline) /: dfgFilters) ((ds, f) => ds.filter(f.valid))
        println(dfgs.map(_.getNodeCount).max)
        println(s"总文件数: $fileCount")
        println(s"总控制流图数: $cfgCount")
        println(s"总数据流图数: ${dfgs.size}")
//        dfgs.foreach(x => {
//            println("=====")
//            println(x.cfg.body)
//            x.print()
//        })
        val temp = dfgs.take(50)
        val result = resultFilter(Miner.mine(temp))
        println("挖掘结束")
        result.foreach(r => {
            println("==========")
            GraphUtil.printGraph(r)
//                        println("---")
//                        temp.find(_.isSuperGraph(r)).foreach(_.cfg.print())
        })
    }
}
