package com.github.woooking.cosyn.util

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
            if (edge.getDirection == Edge.INCOMING) ps.println(edge.getNodeB.getIndex + " -> " + edge.getNodeA.getIndex)
            else ps.println(edge.getNodeA.getIndex + " -> " + edge.getNodeB.getIndex)
        }
    }
}
