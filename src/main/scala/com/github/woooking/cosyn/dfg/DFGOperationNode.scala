package com.github.woooking.cosyn.dfg

import com.github.woooking.cosyn.dfg.DFGOperationNode.OpType

class DFGOperationNode private(val op: OpType.Type, val info: String) extends DFGNode {
    override def equals(obj: scala.Any): Boolean = obj match {
        case n: DFGOperationNode =>
            if (!op.equals(n.op)) return false
            info.equals(n.info)
        case _ =>
            false
    }

    override def hashCode(): Int = op.hashCode ^ info.hashCode

    override def toString: String = s"$op$$$info"
}

object DFGOperationNode {
    def apply(op: OpType.Type, info: String): DFGOperationNode = new DFGOperationNode(op, info)

    def apply(s: String): DFGOperationNode = {
        val p = raw"""#(.*)(?:\$$(.*))?""".r
        s match {
            case p(op, info) => new DFGOperationNode(OpType.withName(op), info)
            case _ => throw new RuntimeException("")
        }
    }

    val ConditionExpr = new DFGOperationNode(OpType.Op, "CONDITION_EXPR")
    val Phi = new DFGOperationNode(OpType.Op, "PHI")
    val ArrayCreation = new DFGOperationNode(OpType.Op, "ARRAY_CREATION")
    val Assignment = new DFGOperationNode(OpType.Op, "ASSIGNMENT")
    val ArrayAccess = new DFGOperationNode(OpType.Op, "ARRAY_ACCESS")
    val Return = new DFGOperationNode(OpType.Op, "RETURN")
    val Assert = new DFGOperationNode(OpType.Op, "ASSERT")

    object OpType extends Enumeration {
        type Type = Value

        val Op: OpType.Value = Value("#OP")
        val MethodInvocation: OpType.Value = Value("#METHOD_INVOCATION")
        val Reference: OpType.Value = Value("#REFERENCE")
        val Binary: OpType.Value = Value("#BINARY")
        val Unary: OpType.Value = Value("#UNARY")
        val FieldAccess: OpType.Value = Value("#FIELD_ACCESS")
        val ArrayCreation: OpType.Value = Value("#ARRAY_CREATION")
        val Extern: OpType.Value = Value("#EXTERN")
    }

}