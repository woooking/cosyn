package com.github.woooking.cosyn.filter

import com.github.woooking.cosyn.dfg.DFG
import com.github.woooking.cosyn.dfg.DFGNode.NodeType

class DataNodeDFGFilter(name: String) extends DFGFilter {
    override def valid(dfg: DFG): Boolean = {
        val nodeIte = dfg.nodeIterator()
        while (nodeIte.hasNext) {
            val node = nodeIte.next()
            if (node.getLabel.op == NodeType.Data && node.getLabel.info == name) return true
        }
        false
    }
}
