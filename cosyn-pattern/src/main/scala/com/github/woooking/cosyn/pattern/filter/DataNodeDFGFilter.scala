package com.github.woooking.cosyn.pattern.filter

import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.DFGNode.NodeType

class DataNodeDFGFilter(name: String) extends DFGFilter {
    override def valid(dfg: SimpleDFG): Boolean = {
        val nodeIte = dfg.nodeIterator()
        while (nodeIte.hasNext) {
            val node = nodeIte.next()
            if (node.getLabel.op == NodeType.Data && node.getLabel.info == name) return true
        }
        false
    }
}
