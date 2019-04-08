package com.github.woooking.cosyn.pattern.javaimpl.dfg

import com.github.woooking.cosyn.pattern.javaimpl.dfg.DFGNode.NodeType

case class DFGDataNode(data: String) extends DFGNode(NodeType.Data, data)
