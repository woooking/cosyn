package com.github.woooking.cosyn

import better.files.File
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.dfg.{DFG, DFGEdge, DFGNode}
import com.github.woooking.cosyn.ir.Visitor
import com.github.woooking.cosyn.javaparser.{CompilationUnit, NodeDelegate}
import com.github.woooking.cosyn.javaparser.body.{BodyDeclaration, MethodDeclaration, VariableDeclarator}
import com.github.woooking.cosyn.javaparser.expr._
import com.github.woooking.cosyn.javaparser.stmt._
import com.github.woooking.cosyn.filter.{CompilationUnitFilter, DFGFilter, FileFilter}
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
        //        val temp = Random.shuffle(dfgs).take(80)
        val temp = dfgs.take(80)
        //        temp.foreach(d => {
        //            val c = d.cfg
        //            println(c.file)
        //            println(c.decl.delegate)
        //            println("###")
        //            c.print()
        //            println("###")
        //            d.print()
        //        })
        val result = resultFilter(Miner.mine(temp)(setting))
        println("挖掘结束")
        result.foreach(r => {
            println("=========")
//            println("pattern:")
//            GraphUtil.printGraph(r)
            val (dfg, (_, nodes)) = temp.toStream.map(d => d -> d.isSuperGraph(r)).filter(_._2._1).head
//            println("original code:")
//            println(dfg.cfg.file)
//            println(dfg.cfg.decl.delegate)
//            println("pattern code:")
            println(generateCode(dfg, nodes))
        })
    }

    def generateCode(dfg: DFG, nodes: Set[DNode]): String = {
        val recoverNodes = dfg.recover(nodes)
        generateCode(dfg.cfg.decl, recoverNodes, Set.empty, "")._1
    }

    def generateCode(node: NodeDelegate[_], nodes: Set[NodeDelegate[_]], names: Set[String], indent: String, noName: Boolean = false): (String, Set[String]) = {
        @inline
        def el(c: String) = if (c == "") "..." else c

        @inline
        def gc1(n1: NodeDelegate[_], idt: String, ctx: Set[String] = names) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, !nodes.contains(node))
            (c1, ctx1)
        }

        @inline
        def gc2(n1: NodeDelegate[_], n2: NodeDelegate[_], idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, notVisitName)
            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt, notVisitName)
            (c1, c2, ctx2)
        }

        @inline
        def gc3(n1: NodeDelegate[_], n2: NodeDelegate[_], n3: NodeDelegate[_], idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt)
            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt)
            val (c3, ctx3) = generateCode(n3, nodes, ctx2, idt)
            (c1, c2, c3, ctx3)
        }

        @inline
        def gcl(ns: List[NodeDelegate[_]], idt: String, ctx: Set[String] = names) = {
            ns.foldLeft((Seq.empty[String], ctx))((context, v) => {
                val (c, ctx) = generateCode(v, nodes, context._2, idt, !nodes.contains(node))
                (context._1 :+ c, ctx)
            })
        }

        @inline
        def rs(code: => String, ctx: Set[String], cs: String*) = {
            if (nodes.contains(node) || cs.exists(_ != "")) (code, ctx)
            else ("", ctx)
        }

        @inline
        def rsn(ctx: Set[String], cs: String*) = {
            (cs.filter(_ != "").mkString("\n"), ctx)
        }

        node match {
            case AssignExpr(NameExpr(name), ope, value) =>
                val (valueCode, ctx) = gc1(value, "")
                val newCtx = if (nodes.contains(value)) ctx + name else ctx
                rs(s"$name $ope $valueCode", newCtx, valueCode)
            case AssignExpr(target, ope, value) =>
                val (targetCode, valueCode, ctx) = gc2(target, value, "")
                rs(s"$targetCode$ope$valueCode", ctx, targetCode, valueCode)
            case ArrayAccessExpr(name, index) =>
                val (nameCode, indexCode, ctx) = gc2(name, index, "")
                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
            case ArrayCreationExpr(ty, _, _) => (s"new $ty[]", names)
            case ArrayInitializerExpr(values) =>
                val (valuesCode, ctx) = gcl(values, "")
                val ellip = valuesCode.map(el).mkString(", ")
                rs(s"{$ellip}", ctx, valuesCode: _*)
            case BinaryExpr(left, ope, right) =>
                val (leftCode, rightCode, ctx) = gc2(left, right, "")
                if (nodes.contains(node)) (s"${el(leftCode)} ${ope.asString()} ${el(rightCode)}", ctx)
                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
                else (s"$leftCode$rightCode", ctx)
            case BlockStmt(s) =>
                val (codes, ctx) = gcl(s, indent)
                (codes.mkString(""), ctx)
            case BooleanLiteralExpr(s) =>
                if (nodes.contains(node)) (s"$s", names)
                else ("", names)
            case CastExpr(ty, expr) =>
                val (code, ctx) = gc1(expr, "")
                rs(s"($ty)$code", ctx, code)
            case ConditionalExpr(condition, thenExpr, elseExpr) =>
                val (conditionCode, thenCode, elseCode, ctx) = gc3(condition, thenExpr, elseExpr, "")
                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
            case ExpressionStmt(s) =>
                val (code, ctx) = gc1(s, "")
                if (code != "") (s"$indent$code;\n", ctx)
                else ("", ctx)
            case FieldAccessExpr(scope, name, _) =>
                val (code, ctx) = gc1(scope, "")
                if (nodes.contains(node)) (s"${el(code)}.$name", ctx)
                else rsn(ctx, code)
            case ForeachStmt(_, _, body) =>
                val (code, ctx) = gc1(body, s"$indent    ")
                rs(s"${indent}for () {\n$code$indent}\n", ctx, code)
            case ForStmt(init, None, update, body) =>
                val (initCode, ctx1) = gcl(init, "")
                val (updateCode, ctx2) = gcl(update, "", ctx1)
                val (bodyCode, ctx3) = generateCode(body, nodes, ctx2, s"$indent    ")
                val codes = initCode ++ updateCode :+ bodyCode
                rs(s"${indent}for (${initCode.mkString("")};; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3, codes: _*)
            case ForStmt(init, Some(compare), update, body) =>
                val (initCode, ctx1) = gcl(init, "")
                val (compareCode, ctx2) = generateCode(compare, nodes, ctx1, s"$indent    ")
                val (updateCode, ctx3) = gcl(update, "", ctx2)
                val (bodyCode, ctx4) = generateCode(body, nodes, ctx3, s"$indent    ")
                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
                rs(s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4, codes: _*)
            case IfStmt(condition, thenStmt, None) =>
                val (conditionCode, ctx1) = generateCode(condition, nodes, names, "")
                val (thenCode, ctx2) = gc1(thenStmt, s"$indent    ", ctx1)
                rs(s"${indent}if ($conditionCode) {\n$thenCode$indent}\n", ctx2, conditionCode, thenCode)
            case IfStmt(condition, thenStmt, Some(elseStmt)) =>
                val (conditionCode, ctx1) = generateCode(condition, nodes, names, "")
                val (thenCode, elseCode, ctx2) = gc2(thenStmt, elseStmt, s"$indent    ", ctx1)
                rs(s"${indent}if ($conditionCode) {\n$thenCode$indent} else {\n$elseCode$indent}\n", ctx2, conditionCode, thenCode)
            case IntegerLiteralExpr(s) =>
                rs(s.toString, names)
            case LongLiteralExpr(s) =>
                rs(s.toString, names)
            case MethodCallExpr(name, None, _, args) =>
                val (argsCode, ctx) = gcl(args, "")
                val ellip = argsCode.map(c => if (c == "") "..." else c).mkString(", ")
                if (nodes.contains(node)) (s"$name($ellip)", ctx)
                else rsn(ctx, argsCode: _*)
            case MethodCallExpr(name, Some(scope), _, args) =>
                val (argsCode, ctx1) = gcl(args, "")
                val (scopeCode, ctx2) = gc1(scope, "", ctx1)
                val ellip = argsCode.map(c => if (c == "") "..." else c).mkString(", ")
                if (nodes.contains(node)) (s"${el(scopeCode)}.$name($ellip)", ctx2)
                else rsn(ctx2, (scopeCode +: argsCode): _*)
            case MethodDeclaration(_, _, _, _, _, _, Some(body)) => generateCode(body, nodes, names, indent)
            case NameExpr(name) =>
                if (nodes.contains(node)) (name, names)
                else if (noName) ("", names)
                else if (names.contains(name)) (name, names)
                else ("", names)
            case NullLiteralExpr() =>
                rs("null", names)
            case ObjectCreationExpr(_, ty, _, args, _) =>
                val (argsCode, ctx) = gcl(args, "")
                val ellip = argsCode.map(c => if (c == "") "..." else c).mkString(", ")
                rs(s"${indent}new $ty($ellip)", ctx, argsCode: _*)
            case ReturnStmt(None) =>
                rs(s"${indent}return;", names)
            case ReturnStmt(Some(expr)) =>
                val (code, ctx) = gc1(expr, "")
                rs(s"return $code;", ctx, code)
            case StringLiteralExpr(s) =>
                rs(s.toString, names)
            case UnaryExpr(ope, expr) =>
                val (code, ctx) = gc1(expr, "")
                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
                rs(c, ctx, code)
            case VariableDeclarationExpr(v) =>
                val (code, ctx) = gcl(v, indent)
                (code.mkString(""), ctx)
            case VariableDeclarator(name, ty, None) =>
                rs(s"$ty $name", names)
            case VariableDeclarator(name, ty, Some(init)) =>
                val (code, ctx) = gc1(init, "")
                val newCtx = if (nodes.contains(node) || nodes.contains(init)) ctx + name else ctx
                rs(s"$ty $name = $code", newCtx, code)
                if (nodes.contains(node) || nodes.contains(init)) (s"$ty $name = $code", ctx + name)
                else rsn(ctx, code)
            case WhileStmt(condition, body) =>
                val (conditionCode, ctx1) = gc1(condition, "")
                val (bodyCode, ctx2) = gc1(body, s"$indent    ", ctx1)
                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
            case TryStmt(_, body, _, _) =>
                val (code, ctx) = gc1(body, indent)
                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
                rs(s"${indent}$code", ctx, code)
            case _ =>
                (s"not implemented $node", names)
        }
    }
}
