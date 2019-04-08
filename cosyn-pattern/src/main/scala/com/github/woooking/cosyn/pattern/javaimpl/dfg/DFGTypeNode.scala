package com.github.woooking.cosyn.pattern.javaimpl.dfg

import com.github.woooking.cosyn.pattern.javaimpl.dfg.DFGNode.NodeType

case class DFGTypeNode(ty: String) extends DFGNode(NodeType.Type, ty)
