package com.github.woooking.cosyn

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.mine.Setting
import de.parsemis.miner.environment.Settings

object Config {
    implicit val setting: Settings[DFGNode, DFGEdge] = Setting.create(DFGNode.parser, DFGEdge.parser, minNodes = 3)
}
