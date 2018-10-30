package com.github.woooking.cosyn.completion

import better.files.File.home
import com.github.woooking.cosyn.JavaParser
import com.github.woooking.cosyn.api.DataSource
import com.github.woooking.cosyn.cosyn.filter._
import com.github.woooking.cosyn.cosyn.transformer.{SeqMapTransformer, TryTransformer}
import com.github.woooking.cosyn.cosyn.Collector
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.dfgprocessor.dfg._
import com.github.woooking.cosyn.javaparser.CompilationUnit
import com.github.woooking.cosyn.mine.{Miner, Setting}
import com.github.woooking.cosyn.util.{GraphTypeDef, GraphUtil}
import de.parsemis.miner.environment.Settings
import de.parsemis.miner.general.IntFrequency
import org.slf4s.Logging

import scala.annotation.tailrec

object Completion extends GraphTypeDef[DFGNode, DFGEdge] with Logging {
    import com.github.woooking.cosyn.Config._

    @tailrec
    private def collectOperationNodes(ite: java.util.Iterator[PNode], result: Seq[DFGNode]): Seq[DFGNode] = {
        if (!ite.hasNext) result
        else {
            val node = ite.next()
            node.getLabel match {
                case n: DFGOperationNode => collectOperationNodes(ite, result :+ n)
                case _ => collectOperationNodes(ite, result)
            }
        }
    }

    def complete(code: String): Unit = {
        val block = JavaParser.parseStatements(code)
//        val visitor = new SimpleVisitor()
//        val cfg = visitor.generateCFG(block)
//        val dfg = SimpleDFG(cfg)
//        val ite = dfg.nodeIterator()
//        val nodes = collectOperationNodes(ite, Seq())
//        complete(nodes)
    }

    def complete(nodes: Seq[DFGNode]): Unit = {
        val dfgNodeFilters = nodes.map(n => DFGNodeFilter(n))
        val fragmentFilters = nodes.map(FragmentFilter)

        val clientCodes = home / "lab" / "java-codes"
        val dataSource = DataSource.fromJavaSourceCodeDir(clientCodes)
        val collector = new Collector[Seq[PFragment]]()

//        dataSource
//            .connect(SeqFilter(FileContentFilter("import java.nio")))
//            .connect(SizeFilter(1200, shuffle = true))
//            .connect(CallbackFilter(s => log.info(s"总数据量： ${s.size}")))
//            .connect(TryTransformer(f => JavaParser.parseFile(f)))
//            .connect(CallbackFilter(s => log.info(s"总控制单元数： ${s.size}")))
//            .connect[Seq[CFGImpl]]((input: Seq[CompilationUnit]) => input.flatMap(new SimpleVisitor().generateCFGs))
//            .connect(CallbackFilter(s => log.info(s"总控制流图数： ${s.size}")))
//            .connect(SeqMapTransformer(SimpleDFG.apply))
//            .connect(CombinedFilter(dfgNodeFilters))
//            .connect(SizeFilter(100, shuffle = true))
//            .connect(CallbackFilter(s => log.info(s"总数据流图数： ${s.size}")))
//            .connect[Seq[PFragment]]((input: Seq[SimpleDFG]) => Miner.mine(input))
//            .connect[Seq[PFragment]]((input: Seq[PFragment]) => input.sortBy(_.frequency().asInstanceOf[IntFrequency].intValue()))
//            .connect(CombinedFilter(fragmentFilters))
//            .connect(collector)

        dataSource.start(())

        val result = collector.get

        result.map(r => r.frequency() -> r.toGraph).foreach(r => {
            println("=====")
            println(r._1)
            GraphUtil.printGraph()(r._2)
        })
    }

    def main(args: Array[String]): Unit = {
        complete(Seq(
            DFGNode(NodeType.MethodInvocation, "remaining")
        ))
    }
}
