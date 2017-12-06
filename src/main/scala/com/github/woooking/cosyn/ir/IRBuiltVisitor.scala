package com.github.woooking.cosyn.ir

import scala.collection.JavaConverters._
import com.github.woooking.cosyn.util.OptionConverters._
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.stmt.{AssertStmt, BreakStmt, CatchClause}
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import com.github.woooking.cosyn.ir.statements._

trait IRBuiltVisitor extends GenericVisitorAdapter[NodeResult, NodeArg] {
    this: IRCode =>

    override def visit(n: ArrayAccessExpr, arg: NodeArg): NodeResult = {
        val array = n.getName.accept(this, arg).asInstanceOf[IRExpression]
        val index = n.getIndex.accept(this, arg).asInstanceOf[IRExpression]
        val target = createTempVariable()
        arg.block.addStatement(IRArrayAccess(target, array, index))
        target
    }

    override def visit(n: ArrayCreationExpr, arg: NodeArg): NodeResult = {
        val sizes = n.getLevels.asScala.map(_.accept(this, arg).asInstanceOf[IRExpression])
        val initializers = n.getInitializer.asScala.map(_.getValues.asScala.map(_.accept(this, arg).asInstanceOf[IRExpression]))
        val target = createTempVariable()
        arg.block.addStatement(IRArrayCreation(target, n.getElementType, sizes, initializers.toSeq.flatten))
        target
    }

    override def visit(n: AssertStmt, arg: NodeArg): NodeResult = {
        val condition = n.getCheck.accept(this, arg).asInstanceOf[IRExpression]
        val message = n.getMessage.asScala.map(_.accept(this, arg).asInstanceOf[IRExpression])
        arg.block.addStatement(IRAssert(condition, message))
        NoResult
    }

    override def visit(n: AssignExpr, arg: NodeArg): NodeResult = {
        val rhs = n.getValue.accept(this, arg).asInstanceOf[IRExpression]
        val lhs = n.getTarget.accept(this, arg).asInstanceOf[IRVariable]
        val statement = n.getOperator match {
            case AssignExpr.Operator.ASSIGN => IRAssignment(lhs, rhs)
            case ope => IRBinaryOperation(lhs, BinaryOperator.fromAssignExprOperator(ope), lhs, rhs)
        }
        arg.block.addStatement(statement)
        lhs
    }

    override def visit(n: BinaryExpr, arg: NodeArg): NodeResult = {
        val lhs = n.getLeft.accept(this, arg).asInstanceOf[IRExpression]
        val rhs = n.getRight.accept(this, arg).asInstanceOf[IRExpression]
        val target = createTempVariable()
        arg.block.addStatement(IRBinaryOperation(target, BinaryOperator.fromBinaryExprOperator(n.getOperator), lhs, rhs))
        target
    }

    override def visit(n: BooleanLiteralExpr, arg: NodeArg): NodeResult = {
        if (n.getValue) IRExpression.True else IRExpression.False
    }

    override def visit(n: BreakStmt, arg: NodeArg): NodeResult = {
        super.visit(n, arg)
        // TODO: Control Flow
    }

    override def visit(n: CatchClause, arg: NodeArg): NodeResult = {
        super.visit(n, arg)
        // TODO: Catch
    }

    override def visit(n: CharLiteralExpr, arg: NodeArg): NodeResult = {
        IRChar(n.getValue.charAt(0))
    }

    override def visit(n: ClassExpr, arg: NodeArg): NodeResult = {
        val target = createTempVariable()
        arg.block.addStatement(IRClassExpr(target, n.getType))
        target
    }

    override def visit(n: ConditionalExpr, arg: NodeArg): NodeResult = {
        val condition = n.getCondition.accept(this, arg).asInstanceOf[IRExpression]
        val thenExpr = n.getThenExpr.accept(this, arg).asInstanceOf[IRExpression]
        val elseExpr = n.getElseExpr.accept(this, arg).asInstanceOf[IRExpression]
        val target = createTempVariable()
        arg.block.addStatement(IRConditionalExpr(target, condition, thenExpr, elseExpr))
        target
    }
}
