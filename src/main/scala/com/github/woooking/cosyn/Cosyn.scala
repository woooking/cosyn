package com.github.woooking.cosyn

import better.files.File
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.dfg.{DFG, DFGEdge, DFGNode}
import com.github.woooking.cosyn.ir.Visitor
import com.github.woooking.cosyn.javaparser.{CompilationUnit, NodeDelegate}
import com.github.woooking.cosyn.javaparser.body.{BodyDeclaration, MethodDeclaration, VariableDeclarator}
import com.github.woooking.cosyn.javaparser.expr._
import com.github.woooking.cosyn.javaparser.stmt._
import com.github.woooking.cosyn.middleware.{CompilationUnitFilter, DFGFilter, FileFilter}
import com.github.woooking.cosyn.mine.{Miner, Setting}
import com.github.woooking.cosyn.util.GraphUtil
import de.parsemis.graph.{Edge, Graph, Node}
import de.parsemis.miner.environment.Settings
import de.parsemis.miner.general.Fragment

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.util.Random

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

    def process()(implicit setting: Settings[DFGNode, DFGEdge]): Unit = {
        val dfgs = (getJavaFilesFromDirectory(dir).flatMap(pipeline) /: dfgFilters) ((ds, f) => ds.filter(f.valid))
        println(s"总文件数: $fileCount")
        println(s"总控制流图数: $cfgCount")
        println(s"总数据流图数: ${dfgs.size}")
        val temp = Random.shuffle(dfgs).take(80)
        temp.map(_.cfg.decl.delegate).foreach(println)
        val result = resultFilter(Miner.mine(temp)(setting))
        println("挖掘结束")
        result.foreach(r => {
            println("=========")
            println("pattern:")
            GraphUtil.printGraph(r)
            val (dfg, (_, nodes)) = temp.toStream.map(d => d -> d.isSuperGraph(r)).filter(_._2._1).head
            println("original code:")
            println(dfg.cfg.decl.delegate)
            println("pattern code:")
            println(generateCode(dfg, nodes))
        })
    }

    def generateCode(dfg: DFG, nodes: Set[DNode]): String = {
        println("=====")
        val recoverNodes = dfg.cfg.recover(nodes)
        generateCode(dfg.cfg.decl, recoverNodes, "")
    }

    def generateCode(node: NodeDelegate[_], nodes: Set[NodeDelegate[_]], indent: String): String = node match {
        case AssignExpr(target, ope, value) =>
            val targetCode = generateCode(target, nodes, "")
            val valueCode = generateCode(value, nodes, "")
            if (nodes.contains(node) || targetCode != "" || valueCode != "") s"$targetCode$ope$valueCode"
            else ""
        //        case ArrayCreationExpr(_, _, _) => "//todo" // Todo
        //        case BinaryExpr(_, _, _) => "//todo" // Todo
        case BlockStmt(s) => s.map(generateCode(_, nodes, indent)).mkString("")
        case ExpressionStmt(s) =>
            val code = generateCode(s, nodes, "")
            if (code != "") s"$indent$code;\n"
            else ""
        case FieldAccessExpr(scope, name, _) =>
            val code = generateCode(scope, nodes, "")
            if (nodes.contains(node)) s"$code.$name"
            else ""
        case ForeachStmt(_, _, body) =>
            val code = generateCode(body, nodes, s"$indent    ")
            if (nodes.contains(node) || code != "") s"${indent}for () {\n$code$indent}\n"
            else ""
        case ForStmt(init, None, update, body) =>
            val initCode = init.map(generateCode(_, nodes, "")).mkString(", ")
            val updateCode = update.map(generateCode(_, nodes, "")).mkString(", ")
            val bodyCode = generateCode(body, nodes, s"$indent    ")
            if (nodes.contains(node) || initCode != "" || updateCode != null || bodyCode != null)
                s"${indent}for ($initCode;; $updateCode) {\n$bodyCode$indent}\n"
            else ""
        case ForStmt(init, Some(compare), update, body) =>
            val initCode = init.map(generateCode(_, nodes, "")).mkString(", ")
            val compareCode = generateCode(compare, nodes, "")
            val updateCode = update.map(generateCode(_, nodes, "")).mkString(", ")
            val bodyCode = generateCode(body, nodes, s"$indent    ")
            if (nodes.contains(node) || initCode != "" || compareCode != null || updateCode != null || bodyCode != null)
                s"${indent}for ($initCode; $compareCode; $updateCode) {\n$bodyCode$indent}\n"
            else ""
        case IfStmt(condition, thenStmt, None) =>
            val conditionCode = generateCode(condition, nodes, "")
            val thenCode = generateCode(thenStmt, nodes, s"$indent    ")
            if (nodes.contains(node) || conditionCode != "" || thenCode != "")
                s"${indent}if ($conditionCode) {\n$thenCode$indent}\n"
            else ""
        case IfStmt(condition, thenStmt, Some(elseStmt)) =>
            val conditionCode = generateCode(condition, nodes, "")
            val thenCode = generateCode(thenStmt, nodes, s"$indent    ")
            val elseCode = generateCode(elseStmt, nodes, s"$indent    ")
            if (nodes.contains(node) || conditionCode != "" || thenCode != "" || elseCode != "")
                s"${indent}if ($conditionCode) {\n$thenCode$indent} else {\n$elseCode$indent}\n"
            else ""
        case IntegerLiteralExpr(s) =>
            if (nodes.contains(node)) s"$s"
            else ""
        case MethodCallExpr(name, _, _, args) =>
            val argsCode = args.map(generateCode(_, nodes, "")).mkString(", ")
            if (nodes.contains(node)) s"$name($argsCode)"
            else ""
        case MethodDeclaration(_, _, _, _, _, _, Some(body)) => generateCode(body, nodes, indent)
        case ObjectCreationExpr(_, ty, _, args, _) =>
            val argsCode = args.map(generateCode(_, nodes, ""))
            val ellip = argsCode.map(c => if (c == "") "..." else c)
            if (nodes.contains(node)) s"${indent}new $ty($ellip)"
            else ""
        case ReturnStmt(_) => "return;"
        case VariableDeclarationExpr(v) => v.map(generateCode(_, nodes, indent)).mkString("")
        case VariableDeclarator(_, _, init) => init.map(generateCode(_, nodes, indent)).mkString("")
        case TryStmt(_, body, _, _) =>
            val code = generateCode(body, nodes, s"$indent    ")
            s"${indent}try {\n$code$indent}\n"
        case _ =>
            s"not implemented $node"
    }
}
