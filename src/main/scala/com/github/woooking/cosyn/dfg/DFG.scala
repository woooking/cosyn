package com.github.woooking.cosyn.dfg

import java.io.PrintStream

import com.github.woooking.cosyn.cfg.{CFG, CFGStatements}
import com.github.woooking.cosyn.ir.statements.IRStatement
import com.github.woooking.cosyn.util.Printable
import com.google.common.collect.{BiMap, HashBiMap}
import de.parsemis.graph._

import scala.collection.JavaConverters._

class DFG(val cfg: CFG) extends ListGraph[DFGNode, DFGEdge] with Printable {
    type DNode = Node[DFGNode, DFGEdge]
    type DGraph = Graph[DFGNode, DFGEdge]

    override def print(ps: PrintStream = System.out): Unit = {
        val ite = edgeIterator();
        while (ite.hasNext()) {
            val edge = ite.next();
            if (edge.getDirection() == Edge.INCOMING) {
                ps.println(edge.getNodeB().getLabel() + " -> " + edge.getNodeA().getLabel())
            } else {
                ps.println(edge.getNodeA().getLabel() + " -> " + edge.getNodeB().getLabel())
            }
        }
    }

    def isSuperGraph(graph: DGraph): (Boolean, Set[DNode]) = {
        val list = graph.nodeIterator().asScala.toList
        list match {
            case Nil => (true, Set.empty)
            case n :: ns => isSuperGraph(HashBiMap.create(), n, ns)
        }
    }

    def isSuperGraph(nodeMap: BiMap[DNode, DNode], current: DNode, remain: List[DNode]): (Boolean, Set[DNode]) = {
        var result: (Boolean, Set[DNode]) = (false, Set.empty)
        val ite = nodeIterator().asScala.filterNot(nodeMap.containsKey).filter(_.getLabel == current.getLabel)
        while (ite.hasNext && !result._1) {
            val node = ite.next()
            var outDiff = false
            val outs = current.outgoingEdgeIterator()
            while (outs.hasNext() && !outDiff) {
                val outEdge = outs.next()
                val other = outEdge.getOtherNode(current)
                if (nodeMap.containsValue(other)) {
                    val mappedDDGBlock = nodeMap.inverse().get(other)
                    if (!node.outgoingEdgeIterator().asScala.map(_.getOtherNode(node)).contains(mappedDDGBlock)) outDiff = true
                }
            }

            var inDiff = false;
            val ins = current.incommingEdgeIterator()
            while (ins.hasNext()) {
                val inEdge = ins.next()
                val other = inEdge.getOtherNode(current)
                if (nodeMap.containsValue(other)) {
                    val mappedDDGBlock = nodeMap.inverse().get(other)
                    if (!mappedDDGBlock.outgoingEdgeIterator().asScala.map(_.getOtherNode(mappedDDGBlock)).contains(node)) inDiff = true
                }
            }

            if (!inDiff && !outDiff) {
                nodeMap.put(node, current)
                remain match {
                    case Nil => result = (true, nodeMap.keySet().asScala.toSet)
                    case n :: ns =>
                        val (is, mapping) = isSuperGraph(nodeMap, n, ns)
                        if (is) result = (true, mapping + node)
                        nodeMap.remove(node)
                }
            }
        }
        result
    }
}

object DFG {
    type DNode = Node[DFGNode, DFGEdge]
    type DEdge = Edge[DFGNode, DFGEdge]

    def apply(cfg: CFG): DFG = {
        val dfg = new DFG(cfg)
        val statements = cfg.blocks.filter(_.isInstanceOf[CFGStatements]).flatMap {
            case block: CFGStatements => block.irStatements ++ block.phis
        }
        val map: Map[IRStatement, Node[DFGNode, DFGEdge]] = statements.map(s => s -> dfg.addNode(DFGNode.statement2node(s))).toMap
        statements.flatMap(s => s.uses.map(use => use -> s)).foreach {
            case (from, to) if from.definition.isDefined =>
                dfg.addEdge(map(from.definition.get), map(to), DFGEdge.singleton, Edge.OUTGOING)
            case (from, to) =>
                dfg.addEdge(dfg.addNode(new DFGDataNode(from.toString)), map(to), DFGEdge.singleton, Edge.OUTGOING)
        }
        dfg
    }
}
