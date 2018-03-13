package com.github.woooking.cosyn.mine

import com.github.woooking.cosyn.dfg.{DFGEdge, DFGNode}
import de.parsemis.graph.ListGraph
import de.parsemis.miner.environment.{Settings => Sett}
import de.parsemis.miner.general.IntFrequency
import de.parsemis.strategy.BFSStrategy

object Setting {
    type N = DFGNode
    type E = DFGEdge

    def create(minFreq: Int = 5) = {
        val s = new Sett[N, E]()
        s.minNodes = 3
        s.minFreq = new IntFrequency(minFreq)
        s.algorithm = new de.parsemis.algorithms.gSpan.Algorithm[N, E]()
        s.strategy = new BFSStrategy[N, E]()
        s.factory = new ListGraph.Factory[N, E](DFGNode.parser, DFGEdge.parser)
        s
    }
}
