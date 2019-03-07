package com.github.woooking.cosyn.pattern.api.filter

import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGNode, SimpleDFG}

/**
  * 一个DFGNodeFilter是对一个<code>Seq[SimpleDFG]</code>过滤的Filter，只保留输入中包含给定[[DFGNode]]的[[SimpleDFG]]
  * @param dfgNode 保留的[[SimpleDFG]]应包含的[[DFGNode]]
  */
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
