package com.github.woooking.cosyn.pattern.dfgprocessor.dfg

import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.{IRArg, IRExpression}
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements._
import de.parsemis.parsers.LabelParser

abstract class DFGNode(val op: NodeType.Type, val info: String) {
    override def equals(obj: scala.Any): Boolean = obj match {
        case n: DFGNode =>
            if (!op.equals(n.op)) return false
            info.equals(n.info)
        case _ =>
            false
    }

    override def hashCode(): Int = op.hashCode ^ info.hashCode

    override def toString: String = s"$op$$$info"
}

object DFGNode {
    val parser: LabelParser[DFGNode] = new LabelParser[DFGNode] {
        override def serialize(labelType: DFGNode): String = labelType.toString

        override def parse(s: String): DFGNode = apply(s)
    }

    def apply(op: NodeType.Type, info: String): DFGOperationNode = new DFGOperationNode(op, info)

    def apply(s: String): DFGNode = {
        val p = raw"""(#.*)(?:\$$(.*))?""".r
        s match {
            case p(op, info) if op == NodeType.Data.toString => new DFGDataNode(info)
            case p(op, info) => new DFGOperationNode(NodeType.withName(op), info)
            case _ => throw new RuntimeException("")
        }
    }

    object NodeType extends Enumeration {
        type Type = Value

        val Op: NodeType.Value = Value("#OP")
        val Data: NodeType.Value = Value("#DATA")
        val MethodInvocation: NodeType.Value = Value("#METHOD_INVOCATION")
        val Reference: NodeType.Value = Value("#REFERENCE")
        val Binary: NodeType.Value = Value("#BINARY")
        val Unary: NodeType.Value = Value("#UNARY")
        val FieldAccess: NodeType.Value = Value("#FIELD_ACCESS")
        val EnumAccess: NodeType.Value = Value("#ENUM_ACCESS")
        val ArrayCreation: NodeType.Value = Value("#ARRAY_CREATION")
        val InstanceOf: NodeType.Value = Value("#INSTANCE_OF")
        val Extern: NodeType.Value = Value("#EXTERN")
    }

    def expression2node(expression: IRExpression): DFGNode = expression match {
        case s: IRArg => new DFGDataNode(s.ty)
        case _ => new DFGDataNode(expression.toString)
    }

    def statement2node(statement: IRStatement): DFGNode = statement match {
        case s: IRBinaryOperation => DFGNode(NodeType.Binary, s.ope.toString)
        case s: IRUnaryOperation => DFGNode(NodeType.Unary, s.ope.toString)
        case s: IREnumAccess => DFGNode(NodeType.EnumAccess, s.ty)
        case s: IRFieldAccess => DFGNode(NodeType.FieldAccess, s.field)
        case s: IRMethodInvocation => DFGNode(NodeType.MethodInvocation, s.name)
        case s: IRInstanceOf => DFGNode(NodeType.InstanceOf, s.ty.toString)
        case _: IRAssignment => DFGOperationNode.Assignment
        case _: IRArrayAccess => DFGOperationNode.ArrayAccess
        case _: IRArrayCreation => DFGOperationNode.ArrayCreation
        case _: IRAssert => DFGOperationNode.Assert
        case _: IRConditionalExpr => DFGOperationNode.ConditionExpr
        case _: IRPhi => DFGOperationNode.Phi
        case _: IRReturn => DFGOperationNode.Return
        case _: IRThrow => DFGOperationNode.Throw
    }
}