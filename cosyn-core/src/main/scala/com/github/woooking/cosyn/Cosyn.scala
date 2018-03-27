package com.github.woooking.cosyn

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.filter.SourceFilter
import com.github.woooking.cosyn.mine.Miner
import com.github.woooking.cosyn.util.GraphTypeDef
import de.parsemis.graph.{Graph => ParsemisGraph}
import de.parsemis.miner.environment.Settings

import scala.collection.mutable

class Cosyn[Data, N, E, Graph <: ParsemisGraph[N, E]]
(source: DataSource[Data], graphGenerator: GraphGenerator[Data, Graph], codeGenerator: CodeGenerator[N, E, Graph]) extends GraphTypeDef[DFGNode, DFGEdge] {

    val sourceFilters = mutable.ArrayBuffer[SourceFilter[Data]]()
    var fileCount = 0
    var cfgCount = 0

//    private def resultFilter(result: Seq[PFragment]): Seq[PGraph] = {
//        val graphs: mutable.ArrayBuffer[PGraph] = mutable.ArrayBuffer()
//        val edgeSets: mutable.ArrayBuffer[Set[PEdge]] = mutable.ArrayBuffer()
//        result.map(_.toGraph).sorted(Ordering.by[Graph[_, _], Int](_.getEdgeCount).reverse).foreach(graph => {
//            val ite = graph.edgeIterator()
//            val edges = ite.asScala.toSet
//            if (!edgeSets.exists(edgeSet => isSubset(edges, edgeSet))) {
//                graphs += graph
//                edgeSets += edges
//            }
//        })
//        graphs
//    }

//    private def isSubset(small: Set[PEdge], big: Set[PEdge]): Boolean = small.headOption match {
//        case None => true
//        case Some(e) =>
//            if (big.exists(bigEdge =>
//                (bigEdge.getNodeA.getLabel == e.getNodeA.getLabel &&
//                    bigEdge.getNodeB.getLabel == e.getNodeB.getLabel &&
//                    bigEdge.getDirection == e.getDirection ||
//                    bigEdge.getNodeA.getLabel == e.getNodeB.getLabel &&
//                        bigEdge.getNodeB.getLabel == e.getNodeA.getLabel &&
//                        bigEdge.getDirection == -e.getDirection) &&
//                    bigEdge.getLabel == e.getLabel)) isSubset(small.tail, big)
//            else false
//    }

    def register(filter: SourceFilter[Data]): Unit = {
        sourceFilters += filter
    }

    def process()(implicit setting: Settings[N, E]): Seq[String] = {
        val data = (source.data /: sourceFilters) ((s, f) => s.filter(f.valid))
        val graphs = graphGenerator.generate(data)
        val temp: Seq[Graph] = graphs.take(80)
        val result = Miner.mine(temp)(setting)
        result.map(codeGenerator.generate(temp))
    }


}
