package com.github.woooking.cosyn.maxkibble

import com.github.woooking.cosyn.CodeGenerator
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.woooking.cosyn.util.GraphTypeDef
import de.parsemis.miner.general.Fragment
import scala.collection.JavaConverters._

case class MaxkibbleGenerator() extends CodeGenerator[DFGNode, DFGEdge, SimpleDFG, (SimpleDFG, java.util.Set[NodeDelegate[_]])] with GraphTypeDef[DFGNode, DFGEdge] {
    override def generate(originalGraph: Seq[SimpleDFG])(fragment: Fragment[DFGNode, DFGEdge]): (SimpleDFG, java.util.Set[NodeDelegate[_]]) = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(fragment.toGraph)).filter(_._2._1).head
        val recovered = dfg.recover(nodes)
        (dfg, recovered.asJava)
    }
}
