package com.github.woooking.cosyn.pattern.api

import com.github.woooking.cosyn.pattern.filter.FragmentFilter
import com.github.woooking.cosyn.pattern.mine.Miner
import com.github.woooking.cosyn.pattern.util.{GraphTypeDef, GraphUtil}
import de.parsemis.graph.{Graph => ParsemisGraph}
import de.parsemis.miner.environment.Settings
import org.slf4s.Logging

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Cosyn[Data, N, E, Graph <: ParsemisGraph[N, E], R]
(source: Data, graphGenerator: GraphGenerator[Data, Graph], codeGenerator: CodeGenerator[N, E, Graph, R], filterSubGraph: Boolean = false)
    extends GraphTypeDef[N, E] with Logging {

    val fragmentFilters: ArrayBuffer[FragmentFilter[N, E]] = mutable.ArrayBuffer()
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

    def register(filter: FragmentFilter[N, E]): Unit = {
        fragmentFilters += filter
    }

    def process()(implicit setting: Settings[N, E]): Seq[R] = {
        val graphs = graphGenerator.generate(source)
        log.info(s"总数据流图数: ${graphs.size}")
        val freqFragments = Miner.mine(graphs)(setting)
        val filteredFragments = (freqFragments /: fragmentFilters) ((ff, f) => ff.filter(f.valid))
        val subFiltered = if (filterSubGraph) resultFilter(filteredFragments) else filteredFragments.map(_.toGraph)
        log.info(s"频繁子图数: ${subFiltered.size}")
        subFiltered.foreach(GraphUtil.printGraph())
        subFiltered.map(codeGenerator.generate(graphs))
    }

}