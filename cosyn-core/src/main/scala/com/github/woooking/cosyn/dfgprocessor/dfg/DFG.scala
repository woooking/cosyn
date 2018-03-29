package com.github.woooking.cosyn.dfgprocessor.dfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfgprocessor.cfg.{CFGImpl, CFGStatements}
import com.github.woooking.cosyn.dfgprocessor.ir.statements.IRStatement
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.woooking.cosyn.util.Printable
import com.google.common.collect.{BiMap, HashBiMap}
import de.parsemis.graph._

import scala.collection.JavaConverters._

class DFG(val cfg: CFGImpl) extends ListGraph[DFGNode, DFGEdge] {
    type DNode = Node[DFGNode, DFGEdge]
    type DGraph = Graph[DFGNode, DFGEdge]

    var map: Map[DNode, Set[NodeDelegate[_]]] = _

    def print(ps: PrintStream = System.out): Unit = {
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

    def recover(nodes: Set[DNode]): Set[NodeDelegate[_]] = {
        nodes.map(map.get).filter(_.nonEmpty).flatMap(_.get)
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

    def apply(cfg: CFGImpl): DFG = {
        val dfg = new DFG(cfg)
        val statements = cfg.blocks.filter(_.isInstanceOf[CFGStatements]).flatMap {
            case block: CFGStatements => block.irStatements ++ block.phis
        }
        val opMap: Map[IRStatement, DNode] = statements.map(s => s -> dfg.addNode(DFGNode.statement2node(s))).toMap
        val dataMap: Map[DNode, Set[NodeDelegate[_]]] = statements.flatMap(s => s.uses.map(use => use -> s)).map {
            case (from, to) if from.definition.isDefined =>
                dfg.addEdge(opMap(from.definition.get), opMap(to), DFGEdge.singleton, Edge.OUTGOING)
                Map.empty[DNode, Set[NodeDelegate[_]]]
            case (from, to) =>
                val node = dfg.addNode(new DFGDataNode(from.toString))
                dfg.addEdge(node, opMap(to), DFGEdge.singleton, Edge.OUTGOING)
                Map(node -> from.fromNode)
        }.foldLeft(Map.empty[DNode, Set[NodeDelegate[_]]])(_ ++ _)
        dfg.map = opMap.map(kv => kv._2 -> kv._1.fromNode) ++ dataMap
        dfg
    }
}
