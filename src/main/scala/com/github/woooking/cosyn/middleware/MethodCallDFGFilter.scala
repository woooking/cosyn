package com.github.woooking.cosyn.middleware

import com.github.woooking.cosyn.dfg.DFG
import com.github.woooking.cosyn.dfg.DFGNode.NodeType

class MethodCallDFGFilter(name: String) extends DFGFilter {
    override def valid(dfg: DFG): Boolean = {
        val nodeIte = dfg.nodeIterator()
        while (nodeIte.hasNext) {
            val node = nodeIte.next()
            if (node.getLabel.op == NodeType.MethodInvocation && node.getLabel.info == name) return true
        }
        false
    }
}
