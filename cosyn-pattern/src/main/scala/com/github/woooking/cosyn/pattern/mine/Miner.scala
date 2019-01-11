package com.github.woooking.cosyn.pattern.mine

import de.parsemis.graph.Graph

import scala.collection.JavaConverters._
import de.parsemis.miner.environment.Settings

object Miner {
    def mine[N, E](dfgs: Seq[Graph[N, E]])(implicit setting: Settings[N, E]) = {
        de.parsemis.Miner.mine[N, E](dfgs.asJavaCollection, setting).asScala.toSeq
    }
}
