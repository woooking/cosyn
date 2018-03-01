package com.github.woooking.cosyn.dfg

import com.github.woooking.cosyn.dfg.DFGNode.NodeType

class DFGOperationNode (op: NodeType.Type, info: String) extends DFGNode(op, info)

object DFGOperationNode {
    val ArrayAccess = new DFGOperationNode(NodeType.Op, "ARRAY_ACCESS")
    val ArrayCreation = new DFGOperationNode(NodeType.Op, "ARRAY_CREATION")
    val Assert = new DFGOperationNode(NodeType.Op, "ASSERT")
    val Assignment = new DFGOperationNode(NodeType.Op, "ASSIGNMENT")
    val ConditionExpr = new DFGOperationNode(NodeType.Op, "CONDITION_EXPR")
    val Phi = new DFGOperationNode(NodeType.Op, "PHI")
    val Return = new DFGOperationNode(NodeType.Op, "RETURN")
    val Throw = new DFGOperationNode(NodeType.Op, "THROW")
}