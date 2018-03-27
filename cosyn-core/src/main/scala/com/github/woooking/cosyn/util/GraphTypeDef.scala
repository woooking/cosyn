package com.github.woooking.cosyn.util

import de.parsemis.graph.{Edge, Graph, Node}
import de.parsemis.miner.general.Fragment

trait GraphTypeDef[N, E] {
    type PNode = Node[N, E]
    type PEdge = Edge[N, E]
    type PGraph = Graph[N, E]
    type PFragment = Fragment[N, E]
}
