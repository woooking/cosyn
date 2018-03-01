package com.github.woooking.cosyn.dfg

import com.github.woooking.cosyn.cfg.{CFG, CFGStatements}
import com.github.woooking.cosyn.ir.IRTemp
import com.github.woooking.cosyn.ir.statements.IRStatement
import de.parsemis.graph.{Edge, Graph, ListGraph, Node}

class DFG {
    val graph = new ListGraph[DFGNode, DFGEdge]()
}

object DFG {
    type DNode = Node[DFGNode, DFGEdge]
    type DEdge = Edge[DFGNode, DFGEdge]

    def apply(cfg: CFG): DFG = {
        val dfg = new DFG
        val statements = cfg.blocks.filter(_.isInstanceOf[CFGStatements]).flatMap {
            case block: CFGStatements => block.statements ++ block.phis
        }
        val map: Map[IRStatement, Node[DFGNode, DFGEdge]] = statements.map(s => s -> dfg.graph.addNode(DFGNode.statement2node(s))).toMap
        statements.flatMap(s => s.uses.map(use => use -> s)).foreach {
            case (from, to) if from.definition.isDefined =>
                dfg.graph.addEdge(map(from.definition.get), map(to), DFGEdge.singleton, Edge.OUTGOING)
            case (from, to) =>
                dfg.graph.addEdge(dfg.graph.addNode(new DFGDataNode(from.toString)), map(to), DFGEdge.singleton, Edge.OUTGOING)
        }
        dfg
    }
}
