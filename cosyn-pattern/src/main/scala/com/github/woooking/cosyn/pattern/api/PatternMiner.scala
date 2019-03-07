package com.github.woooking.cosyn.pattern.api

import com.github.woooking.cosyn.pattern.util.{GraphTypeDef, GraphUtil}
import de.parsemis.graph.{Graph => ParsemisGraph}
import de.parsemis.miner.environment.Settings
import org.slf4s.Logging

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * 代码挖掘API类
  * @param source 输入数据
  * @param graphGenerator 图生成器
  * @param patternGenerator 代码生成器
  * @param filterSubGraph 是否过滤子图
  * @tparam Data 数据类型
  * @tparam N 结点类型
  * @tparam E 边类型
  * @tparam Graph 图类型
  * @tparam R 输出代码模式类型
  */
class PatternMiner[Data, N, E, Graph <: ParsemisGraph[N, E], R]
(source: Data, graphGenerator: GraphGenerator[Data, Graph], patternGenerator: PatternGenerator[N, E, Graph, R], filterSubGraph: Boolean = false)
    extends GraphTypeDef[N, E] with Logging {

    var fileCount = 0
    var cfgCount = 0

    private def resultFilter(result: Seq[PFragment]): Seq[PGraph] = {
        val graphs: mutable.ArrayBuffer[PGraph] = mutable.ArrayBuffer()
        val edgeSets: mutable.ArrayBuffer[Set[PEdge]] = mutable.ArrayBuffer()
        result.map(_.toGraph).sorted(Ordering.by[ParsemisGraph[_, _], Int](_.getEdgeCount).reverse).foreach(graph => {
            val ite = graph.edgeIterator()
            val edges = ite.asScala.toSet
            if (!edgeSets.exists(edgeSet => isSubset(edges, edgeSet))) {
                graphs += graph
                edgeSets += edges
            }
        })
        graphs
    }

    @tailrec
    private def isSubset(small: Set[PEdge], big: Set[PEdge]): Boolean = small.headOption match {
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

    def process()(implicit setting: Settings[N, E]): Seq[R] = {
        val graphs = graphGenerator.generate(source)
        log.info(s"总数据流图数: ${graphs.size}")
        val freqFragments = mine(graphs)
        val subFiltered = if (filterSubGraph) resultFilter(freqFragments) else freqFragments.map(_.toGraph)
        log.info(s"频繁子图数: ${subFiltered.size}")
        subFiltered.foreach(GraphUtil.printGraph())
        subFiltered.map(patternGenerator.generate(graphs))
    }

    private def mine(dfgs: Seq[ParsemisGraph[N, E]])(implicit setting: Settings[N, E]) = {
        de.parsemis.Miner.mine[N, E](dfgs.asJavaCollection, setting).asScala.toSeq
    }
}
