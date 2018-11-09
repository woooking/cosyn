package com.github.woooking.cosyn.api.filter

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGNode, SimpleDFG}

case class DFGNodeFilter(dfgNode: DFGNode) extends SeqFilter[SimpleDFG] {
    def valid(dfg: SimpleDFG): Boolean = {
        val nodeIte = dfg.nodeIterator()
        while (nodeIte.hasNext) {
            val node = nodeIte.next()
            if (node.getLabel.op == dfgNode.op && node.getLabel.info == dfgNode.info) return true
        }
        false
    }
}
