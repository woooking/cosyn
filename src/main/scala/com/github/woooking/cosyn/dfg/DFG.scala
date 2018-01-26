package com.github.woooking.cosyn.dfg

import com.github.woooking.cosyn.cfg.{CFG, Statements}
import com.github.woooking.cosyn.ir.IRVariable
import com.github.woooking.cosyn.ir.statements.IRStatement
import de.parsemis.graph.{Edge, ListGraph, Node}

class DFG {
    val graph = new ListGraph[DFGNode, DFGEdge]()
}

object DFG {
    type DNode = Node[DFGNode, DFGEdge]
    type DEdge = Edge[DFGNode, DFGEdge]

    def apply(cfg: CFG): DFG = {
        val dfg = new DFG
        val statements = cfg.blocks.filter(_.isInstanceOf[Statements]).flatMap {
            case block: Statements => block.statements
        }
        val map: Map[IRStatement, Node[DFGNode, DFGEdge]] = statements.map(s => s -> dfg.graph.addNode(DFGNode.statement2node(s))).toMap
        statements.flatMap(s => s.uses.map(use => s -> use)).foreach {
            case (from, to: IRVariable) =>
//                dfg.graph.addEdge(map(from), map(to), new DFGEdge {}, Edge.OUTGOING)
            case (from, to) =>
        }
        println(map)
        dfg
    }
}
