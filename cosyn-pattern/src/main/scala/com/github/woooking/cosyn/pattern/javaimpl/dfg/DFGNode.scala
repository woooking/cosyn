package com.github.woooking.cosyn.pattern.javaimpl.dfg

import com.github.woooking.cosyn.pattern.Components
import com.github.woooking.cosyn.pattern.javaimpl.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements._
import com.github.woooking.cosyn.pattern.javaimpl.ir._
import de.parsemis.parsers.LabelParser
import org.slf4s.Logging

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

object DFGNode extends Logging {
    private val typeEntityRepository = Components.typeEntityRepository

    val parser: LabelParser[DFGNode] = new LabelParser[DFGNode] {
        override def serialize(labelType: DFGNode): String = labelType.toString

        override def parse(s: String): DFGNode = apply(s)
    }

    def apply(op: NodeType.Type, info: String): DFGOperationNode = new DFGOperationNode(op, info)

    def apply(s: String): DFGNode = {
        val p = raw"""(#.*)(?:\$$(.*))?""".r
        s match {
            case p(op, info) if op == NodeType.Data.toString => DFGDataNode(info)
            case p(op, info) => new DFGOperationNode(NodeType.withName(op), info)
            case _ => throw new RuntimeException("")
        }
    }

    object NodeType extends Enumeration {
        type Type = Value

        val Op: NodeType.Value = Value("#OP")
        val Data: NodeType.Value = Value("#DATA")
        val Type: NodeType.Value = Value("#TYPE")
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

    def expression2DataNode(expression: IRExpression): DFGNode = expression match {
        case s: IRArg => DFGDataNode(s.name)
        case _ => DFGDataNode(expression.toString)
    }

    def expression2Type(expression: IRExpression): Set[DFGTypeNode] = expression match {
        case _: IRString => Set(DFGTypeNode("java.lang.String"))
        case _: IRInteger => Set(DFGTypeNode("int"))
        case _: IRLong => Set(DFGTypeNode("long"))
        case _: IRDouble => Set(DFGTypeNode("double"))
        case _: IRBoolean => Set(DFGTypeNode("boolean"))
        case _: IRChar => Set(DFGTypeNode("char"))
        case _: IRNull => Set(DFGTypeNode("java.lang.Object"))
        case e: IRExtern => typeEntityRepository.getAllParentTypes(e.ty).map(DFGTypeNode)
        case e: IRArg => typeEntityRepository.getAllParentTypes(e.ty).map(DFGTypeNode)
        case e: IRTemp => expression2Type(e.replaced.get)
        case e: IREnum => typeEntityRepository.getAllParentTypes(e.ty).map(DFGTypeNode)
        case _: IRTypeObject => Set(DFGTypeNode("java.lang.Class"))
        case _: IRThis => Set(DFGTypeNode("this"))
        case _: IRArray => Set(DFGTypeNode("array"))
        case _: IRSuper => Set(DFGTypeNode("super"))
        case _ =>
            log.error(s"${expression.getClass} missing")
            ???
    }

    def statement2OpNode(statement: IRStatement): DFGNode = statement match {
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

    def statement2Type(statement: IRStatement): Set[DFGTypeNode] = statement match {
        case s: IRBinaryOperation => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IRUnaryOperation => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IREnumAccess => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IRFieldAccess => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IRMethodInvocation => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case _: IRInstanceOf => Set(DFGTypeNode("boolean"))
        case s: IRAssignment => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IRArrayAccess => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IRArrayCreation => typeEntityRepository.getAllParentTypes(s.ty.asString()).map(DFGTypeNode)
        case s: IRConditionalExpr => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case s: IRPhi => typeEntityRepository.getAllParentTypes(s.ty).map(DFGTypeNode)
        case _ =>
            log.error(s"${statement.getClass} missing")
            ???
    }
}