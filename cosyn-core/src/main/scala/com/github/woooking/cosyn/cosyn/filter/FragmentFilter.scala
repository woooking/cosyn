package com.github.woooking.cosyn.cosyn.filter

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode}
import de.parsemis.miner.general.Fragment

class FragmentFilter(dfgNode: DFGNode) extends SeqFilter[Fragment[DFGNode, DFGEdge]] {
    def valid(fragment: Fragment[DFGNode, DFGEdge]): Boolean = {
        val graph = fragment.toGraph
        val nodeIte = graph.nodeIterator()
        while (nodeIte.hasNext) {
            val node = nodeIte.next()
            if (node.getLabel.op == dfgNode.op && node.getLabel.info == dfgNode.info) return true
        }
        false
    }

    override def valid(data: Seq[Fragment[DFGNode, DFGEdge]]): Seq[Fragment[DFGNode, DFGEdge]] = data.filter(valid)
}
