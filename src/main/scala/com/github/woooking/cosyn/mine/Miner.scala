package com.github.woooking.cosyn.mine

import com.github.woooking.cosyn.dfg.{DFG, DFGEdge, DFGNode}
import de.parsemis.graph.Graph

import scala.collection.JavaConverters._
import de.parsemis.miner.environment.Settings

object Miner {
    def mine(dfgs: Seq[DFG])(implicit setting: Settings[DFGNode, DFGEdge]) = {
        de.parsemis.Miner.mine[DFGNode, DFGEdge](dfgs.map(_.asInstanceOf[Graph[DFGNode, DFGEdge]]).asJavaCollection, setting).asScala.toSeq
    }
}
