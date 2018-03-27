package com.github.woooking.cosyn.util

import java.io.PrintStream

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode}
import de.parsemis.graph.{Edge, Graph}

object GraphUtil {
    def printGraph(g: Graph[DFGNode, DFGEdge], ps: PrintStream = System.out): Unit = {
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
