package com.github.woooking.cosyn.pattern.api

import de.parsemis.graph.ListGraph
import de.parsemis.miner.environment.{Statistics, Settings}
import de.parsemis.miner.general.IntFrequency
import de.parsemis.parsers.LabelParser
import de.parsemis.strategy.ThreadedDFSStrategy

object Setting {
    def create[N, E](nodeParser: LabelParser[N], edgeParser: LabelParser[E], minFreq: Int = 3, minNodes: Int = 7,
                     closeGraph: Boolean = true): Settings[N, E] = {
        val s = new Settings[N, E]()
        s.minNodes = minNodes
        s.minFreq = new IntFrequency(minFreq)
        s.algorithm = new de.parsemis.algorithms.gSpan.Algorithm[N, E]()
        s.threadCount = 8
        s.strategy = new ThreadedDFSStrategy[N, E](8, new Statistics)
        s.factory = new ListGraph.Factory[N, E](nodeParser, edgeParser)
        s.closeGraph = closeGraph
        s
    }
}