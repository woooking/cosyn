package com.github.woooking.cosyn.dfgprocessor.dfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfgprocessor.cfg.{CFGImpl, CFGStatements}
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.dfgprocessor.ir.statements.IRStatement
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.woooking.cosyn.util.Printable
import com.google.common.collect.{BiMap, HashBiMap}
import de.parsemis.graph._

import scala.annotation.tailrec
import scala.collection.JavaConverters._

class SimpleDFG(val cfg: CFGImpl) extends ListGraph[DFGNode, DFGEdge] {
    type DNode = Node[DFGNode, DFGEdge]
    type DGraph = Graph[DFGNode, DFGEdge]

    var map: Map[DNode, Set[NodeDelegate[_]]] = _

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

object SimpleDFG {
    implicit val dfgPrintable = new Printable[SimpleDFG] {
        override def print(obj: SimpleDFG, ps: PrintStream): Unit = {
            val ite = obj.edgeIterator();
            while (ite.hasNext()) {
                val edge = ite.next();
                if (edge.getDirection() == Edge.INCOMING) {
                    ps.println(edge.getNodeB().getLabel() + " -> " + edge.getNodeA().getLabel())
                } else {
                    ps.println(edge.getNodeA().getLabel() + " -> " + edge.getNodeB().getLabel())
                }
            }
        }
    }

    type DNode = Node[DFGNode, DFGEdge]
    type DEdge = Edge[DFGNode, DFGEdge]


    def apply(cfg: CFGImpl): SimpleDFG = {
        val dfg = new SimpleDFG(cfg)
        val statements = cfg.blocks.filter(_.isInstanceOf[CFGStatements]).flatMap {
            case block: CFGStatements => block.irStatements ++ block.phis
        }
        val opMap: Map[IRStatement, DNode] = statements.map(s => s -> dfg.addNode(DFGNode.statement2node(s))).toMap

        @tailrec
        def build(statements: List[(IRExpression, IRStatement)], dataNodes: Map[String, DNode], dataMap: Map[DNode, Set[NodeDelegate[_]]]): Map[DNode, Set[NodeDelegate[_]]] = statements match {
            case Nil => dataMap
            case s :: ss =>
                val (newDataNodes, newDataMap) = s match {
                    case (from, to) if from.definition.isDefined =>
                        dfg.addEdge(opMap(from.definition.get), opMap(to), DFGEdge.singleton, Edge.OUTGOING)
                        (dataNodes, dataMap)
                    case (from, to) =>
                        val node = dataNodes.getOrElse(from.toString, dfg.addNode(DFGNode.expression2node(from)))
                        dfg.addEdge(node, opMap(to), DFGEdge.singleton, Edge.OUTGOING)
                        val fromNodes = dataMap.getOrElse(node, Set.empty[NodeDelegate[_]])
                        (dataNodes.updated(from.toString, node), dataMap.updated(node, fromNodes ++ from.fromNodes))
                }
                build(ss, newDataNodes, newDataMap)
        }

        val dataMap: Map[DNode, Set[NodeDelegate[_]]] = build(
            statements.flatMap(s => s.uses.map(use => use -> s)).toList,
            Map.empty,
            Map.empty
        )
        dfg.map = opMap.map(kv => kv._2 -> kv._1.fromNode) ++ dataMap
        dfg
    }
}
