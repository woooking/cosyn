package com.github.woooking.cosyn.pattern.util

import java.io.PrintStream

import de.parsemis.graph.{Edge, Graph, Node}

object GraphUtil {
    def fromNode[N, E](edge: Edge[N, E]): Node[N, E] = {
        if (edge.getDirection == Edge.OUTGOING) edge.getNodeA
        else edge.getNodeB
    }

    def toNode[N, E](edge: Edge[N, E]): Node[N, E] = {
        if (edge.getDirection == Edge.INCOMING) edge.getNodeA
        else edge.getNodeB
    }

    def printGraph(ps: PrintStream = System.out)(g: Graph[_, _]): Unit = {
        val nodeIte = g.nodeIterator
        while ( {nodeIte.hasNext}) {
            val node = nodeIte.next
            ps.println(s"Node ${node.getIndex}: ${node.getLabel}")
        }
        val ite = g.edgeIterator
        while ( {ite.hasNext}) {
            val edge = ite.next
            if (edge.getDirection == Edge.INCOMING) ps.println(s"${edge.getNodeB.getIndex} -- ${edge.getLabel} -> ${edge.getNodeA.getIndex}")
            else ps.println(s"${edge.getNodeA.getIndex} -- ${edge.getLabel} -> ${edge.getNodeB.getIndex}")
        }
    }
}
