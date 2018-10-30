package com.github.woooking.cosyn.lab

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.CodeGenerator
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.util.GraphTypeDef

import scala.collection.JavaConverters._

case class MaxkibbleGenerator() extends CodeGenerator[DFGNode, DFGEdge, SimpleDFG, (SimpleDFG, java.util.Set[Node])] with GraphTypeDef[DFGNode, DFGEdge] {
    override def generate(originalGraph: Seq[SimpleDFG])(graph: PGraph): (SimpleDFG, java.util.Set[Node]) = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(graph)).filter(_._2._1).head
        val recovered = dfg.recover(nodes)
        (dfg, recovered.asJava)
    }
}
