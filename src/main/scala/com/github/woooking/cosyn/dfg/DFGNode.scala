package com.github.woooking.cosyn.dfg

import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.dfg.DFGOperationNode.OpType
import com.github.woooking.cosyn.ir.statements._

trait DFGNode

object DFGNode {
    def statement2node(statement: IRStatement): DFGNode = statement match {
        case s: IRBinaryOperation => DFGOperationNode(OpType.Binary, s.ope.toString)
        case s: IRUnaryOperation => DFGOperationNode(OpType.Unary, s.ope.toString)
        case s: IRFieldAccess => DFGOperationNode(OpType.FieldAccess, s.field)
        case s: IRMethodInvocation => DFGOperationNode(OpType.MethodInvocation, s.name)
        case _: IRConditionalExpr => DFGOperationNode.ConditionExpr
        case _: CFG#IRPhi => DFGOperationNode.Phi
        case _: IRArrayCreation => DFGOperationNode.ArrayCreation
        case _: IRReturn => DFGOperationNode.ConditionExpr
        case _: IRArrayAccess => DFGOperationNode.ArrayAccess
        case _: IRAssert => DFGOperationNode.Assert
        case _: IRAssignment => DFGOperationNode.Assignment
    }
}