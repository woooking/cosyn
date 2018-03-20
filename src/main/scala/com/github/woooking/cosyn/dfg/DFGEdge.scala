package com.github.woooking.cosyn.dfg

import com.github.woooking.cosyn.dfg.DFGEdge.EdgeType
import de.parsemis.parsers.LabelParser

class DFGEdge(val op: EdgeType.Type, val info: String) {
    override def equals(obj: scala.Any): Boolean = obj match {
        case n: DFGEdge =>
            if (!op.equals(n.op)) return false
            info.equals(n.info)
        case _ =>
            false
    }

    override def hashCode(): Int = op.hashCode ^ info.hashCode

    override def toString: String = s"$op$$$info"
}

object DFGEdge {
    val singleton = new DFGEdge(EdgeType.Singleton, "empty")

    val parser = new LabelParser[DFGEdge] {
        override def serialize(labelType: DFGEdge): String = labelType.toString

        override def parse(s: String): DFGEdge = apply(s)
    }

    def apply(op: EdgeType.Type, info: String): DFGEdge = new DFGEdge(op, info)

    def apply(s: String): DFGEdge = {
        val p = raw"""(#.*)(?:\$$(.*))?""".r
        s match {
            case p(op, _) if op == EdgeType.Singleton.toString => singleton
            case p(op, info) => new DFGEdge(EdgeType.withName(op), info)
            case _ => throw new RuntimeException("")
        }
    }

    object EdgeType extends Enumeration {
        type Type = Value

        val Singleton: EdgeType.Value = Value("#None")
        val Data: EdgeType.Value = Value("#DATA")
        val MethodInvocation: EdgeType.Value = Value("#METHOD_INVOCATION")
        val Reference: EdgeType.Value = Value("#REFERENCE")
        val Binary: EdgeType.Value = Value("#BINARY")
        val Unary: EdgeType.Value = Value("#UNARY")
        val FieldAccess: EdgeType.Value = Value("#FIELD_ACCESS")
        val ArrayCreation: EdgeType.Value = Value("#ARRAY_CREATION")
        val InstanceOf: EdgeType.Value = Value("#INSTANCE_OF")
        val Extern: EdgeType.Value = Value("#EXTERN")
    }
}