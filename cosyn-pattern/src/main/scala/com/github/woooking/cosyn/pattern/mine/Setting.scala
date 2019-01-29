package com.github.woooking.cosyn.pattern.mine

import de.parsemis.graph.ListGraph
import de.parsemis.miner.environment.{Statistics, Settings => Sett}
import de.parsemis.miner.general.IntFrequency
import de.parsemis.parsers.LabelParser
import de.parsemis.strategy.ThreadedDFSStrategy

object Setting {
    def create[N, E](nodeParser: LabelParser[N], edgeParser: LabelParser[E], minFreq: Int = 3, minNodes: Int = 4,
                     closeGraph: Boolean = true) = {
        val s = new Sett[N, E]()
        s.minNodes = minNodes
        s.minFreq = new IntFrequency(minFreq)
        s.algorithm = new de.parsemis.algorithms.gSpan.Algorithm[N, E]()
//        s.strategy = new BFSStrategy[N, E]()
        s.threadCount = 8
        s.strategy = new ThreadedDFSStrategy[N, E](8, new Statistics)
        s.factory = new ListGraph.Factory[N, E](nodeParser, edgeParser)
        s.closeGraph = closeGraph
        s
    }
}
