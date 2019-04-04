package com.github.woooking.cosyn.pattern.javaimpl.dfg

import cats.Show
import com.github.javaparser.ast.{Node => ASTNode}
import com.github.woooking.cosyn.pattern.javaimpl.cfg.{CFG, CFGStatements}
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.{IRDefStatement, IRStatement}
import com.github.woooking.cosyn.pattern.util.GraphTypeDef
import com.google.common.collect.{BiMap, HashBiMap}
import de.parsemis.graph._

import scala.annotation.tailrec
import scala.collection.JavaConverters._

class SimpleDFG(val cfg: CFG) extends ListGraph[DFGNode, DFGEdge] with GraphTypeDef[DFGNode, DFGEdge] {
    var map: Map[PNode, Set[ASTNode]] = _

    def recover(nodes: Set[PNode]): Set[ASTNode] = {
        nodes.map(map.get).filter(_.nonEmpty).flatMap(_.get)
    }

    def isSuperGraph(graph: PGraph): (Boolean, Set[PNode]) = {
        val list = graph.nodeIterator().asScala.toList
        list match {
            case Nil => (true, Set.empty)
            case n :: ns => isSuperGraph(HashBiMap.create(), n, ns)
        }
    }

    def isSuperGraph(nodeMap: BiMap[PNode, PNode], current: PNode, remain: List[PNode]): (Boolean, Set[PNode]) = {
        var result: (Boolean, Set[PNode]) = (false, Set.empty)
        val ite = nodeIterator().asScala.filterNot(nodeMap.containsKey).filter(_.getLabel == current.getLabel)
        while (ite.hasNext && !result._1) {
            val node = ite.next()
            var outDiff = false
            val outs = current.outgoingEdgeIterator()
            while (outs.hasNext && !outDiff) {
                val outEdge = outs.next()
                val other = outEdge.getOtherNode(current)
                if (nodeMap.containsValue(other)) {
                    val mappedDDGBlock = nodeMap.inverse().get(other)
                    if (!node.outgoingEdgeIterator().asScala.map(_.getOtherNode(node)).contains(mappedDDGBlock)) outDiff = true
                }
            }

            var inDiff = false
            val ins = current.incommingEdgeIterator()
            while (ins.hasNext) {
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

object SimpleDFG extends GraphTypeDef[DFGNode, DFGEdge] {
    implicit val dfgShow: Show[SimpleDFG] = (obj: SimpleDFG) => {
        var s = ""
        val ite = obj.edgeIterator()
        while (ite.hasNext) {
            val edge = ite.next()
            if (edge.getDirection() == Edge.INCOMING) {
                s = s + s"${edge.getNodeB.getLabel} -- ${edge.getLabel} -> ${edge.getNodeA.getLabel}\n"
            } else {
                s = s + s"${edge.getNodeA.getLabel} -- ${edge.getLabel} -> ${edge.getNodeB.getLabel}\n"
            }
        }
        s
    }

    def apply(cfg: CFG): SimpleDFG = {
        val dfg = new SimpleDFG(cfg)
        val statements = cfg.blocks.filter(_.isInstanceOf[CFGStatements]).flatMap {
            case block: CFGStatements => block.irStatements ++ block.phis
        }

        val nodes: Seq[(IRStatement, PNode, Set[PNode])] = statements.map(s => {
            val opNode = dfg.addNode(DFGNode.statement2OpNode(s))
            if (s.isInstanceOf[IRDefStatement]) {
                val tyNodes = DFGNode.statement2Type(s).map(n => {
                    val tyNode = dfg.addNode(n)
                    dfg.addEdge(opNode, tyNode, DFGEdge.singleton, Edge.OUTGOING)
                    tyNode
                })
                (s, opNode, tyNodes)
            } else (s, opNode, Set.empty[PNode])
        })

        val opMap: Map[IRStatement, PNode] = nodes.map(p => p._1 -> p._2).toMap
        val tyMap: Map[IRStatement, Set[PNode]] = nodes.map(p => p._1 -> p._3).toMap

        @tailrec
        def build(statements: List[(IRExpression, IRStatement)], dataNodes: Map[IRExpression, Set[PNode]], dataMap: Map[PNode, Set[ASTNode]]): Map[PNode, Set[ASTNode]] = statements match {
            case Nil => dataMap
            case s :: ss =>
                val (newDataNodes, newDataMap) = s match {
                    case (from, to) if from.definition.isDefined =>
                        tyMap(from.definition.get).foreach(dfg.addEdge(_, opMap(to), DFGEdge.singleton, Edge.OUTGOING))
                        (dataNodes, dataMap)
                    case (from, to) =>
                        val (newMap, newDataMap, tyNodes) = if (dataNodes.contains(from)) (dataNodes, dataMap, dataNodes(from)) else {
                            val dataNode = dfg.addNode(DFGNode.expression2DataNode(from))
                            val tyNodes = DFGNode.expression2Type(from).map(n => {
                                val tyNode = dfg.addNode(n)
                                dfg.addEdge(dataNode, tyNode, DFGEdge.singleton, Edge.OUTGOING)
                                tyNode
                            })
                            (dataNodes.updated(from, tyNodes), dataMap.updated(dataNode, from.fromNodes), tyNodes)
                        }
                        tyNodes.foreach(dfg.addEdge(_, opMap(to), DFGEdge.singleton, Edge.OUTGOING))
                        (newMap, newDataMap)
                }
                build(ss, newDataNodes, newDataMap.asInstanceOf[Map[PNode, Set[ASTNode]]])
        }

        val dataMap: Map[PNode, Set[ASTNode]] = build(
            statements.flatMap(s => s.uses.map(use => use -> s)).toList,
            Map.empty,
            Map.empty
        )
        dfg.map = opMap.map(kv => kv._2 -> kv._1.fromNode) ++ dataMap
        dfg
    }
}
