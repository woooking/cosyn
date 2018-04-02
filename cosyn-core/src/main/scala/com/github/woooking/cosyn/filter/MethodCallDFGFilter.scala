package com.github.woooking.cosyn.filter

import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.dfgprocessor.dfg.DFGNode.NodeType

class MethodCallDFGFilter(name: String) extends DFGFilter {
    override def valid(dfg: SimpleDFG): Boolean = {
        val nodeIte = dfg.nodeIterator()
        while (nodeIte.hasNext) {
            val node = nodeIte.next()
            if (node.getLabel.op == NodeType.MethodInvocation && node.getLabel.info == name) return true
        }
        false
    }
}
