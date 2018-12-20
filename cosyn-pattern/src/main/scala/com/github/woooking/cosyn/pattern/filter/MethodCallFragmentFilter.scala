package com.github.woooking.cosyn.pattern.filter

import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGEdge, DFGNode}
import de.parsemis.graph.Node
import de.parsemis.miner.general.Fragment

import scala.annotation.tailrec

class MethodCallFragmentFilter(name: String) extends FragmentFilter[DFGNode, DFGEdge] {
    override def valid(fragment: Fragment[DFGNode, DFGEdge]): Boolean = {
        val ite = fragment.toGraph.nodeIterator()
        validNode(ite)
    }

    @tailrec
    private def validNode(ite: java.util.Iterator[Node[DFGNode, DFGEdge]]): Boolean = {
        if (ite.hasNext) {
            val node = ite.next()
            if (node.getLabel.op == NodeType.MethodInvocation && node.getLabel.info == name) true
            else validNode(ite)
        } else false
    }
}
