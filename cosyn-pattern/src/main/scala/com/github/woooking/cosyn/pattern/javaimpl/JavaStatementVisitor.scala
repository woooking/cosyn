package com.github.woooking.cosyn.pattern.javaimpl

import com.github.javaparser.ast.stmt._
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults
import com.github.javaparser.ast.{Node, NodeList}
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFGSwitch.{DefaultLabel, ExpressionLabel}
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.{IRAssert, IRMethodInvocation, IRReturn, IRThrow}
import org.slf4s.Logging

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

class JavaStatementVisitor(val outerCfg: CFG) extends Logging {
    private val exprVisitor = new JavaExpressionVisitor(outerCfg)

    val visitor: GenericVisitorWithDefaults[outerCfg.Context, outerCfg.Context] = new GenericVisitorWithDefaults[outerCfg.Context, outerCfg.Context] {
        override def visit(n: AssertStmt, context: outerCfg.Context): outerCfg.Context = {
            for (
                check <- n.getCheck.accept(exprVisitor, context.block);
                message <- n.getMessage.asScala.map(_.accept(exprVisitor, context.block))
            ) context.block.addStatement(new IRAssert(check, message, Set(n)))
            context
        }

        override def visit(blockStmt: BlockStmt, context: outerCfg.Context): outerCfg.Context = {
            (context /: blockStmt.getStatements.asScala) ((c, n) => n.accept(this, c))
        }

        override def visit(n: BreakStmt, context: outerCfg.Context): outerCfg.Context = {
            // TODO: change control flow
            log.warn("BreakStmt needs to be enhanced")
            context
        }

        override def visit(n: ContinueStmt, context: outerCfg.Context): outerCfg.Context = {
            // TODO: change control flow
            log.warn("ContinueStmt needs to be enhanced")
            context
        }

        override def visit(n: DoStmt, context: outerCfg.Context): outerCfg.Context = {
            context.block.seal()
            val entryBlock = outerCfg.createStatements()
            val bodyBlock = outerCfg.createStatements()
            val exitBlock = outerCfg.createStatements()
            val compareBlock = outerCfg.createStatements()
            context.block.setNext(entryBlock)
            entryBlock.setNext(bodyBlock)
            val newContext = n.getBody.accept(this, context.push(bodyBlock, exitBlock, compareBlock))
            newContext.block.seal()
            newContext.block.setNext(compareBlock)
            val compareResult = n.getCondition.accept(exprVisitor, compareBlock)
            compareBlock.seal()
            val branch = outerCfg.createBranch(compareResult, bodyBlock, exitBlock)
            compareBlock.setNext(branch)
            branch.seal()
            entryBlock.seal()
            newContext.pop(exitBlock)
        }

        override def visit(n: EmptyStmt, context: outerCfg.Context): outerCfg.Context = {
            context
        }

        override def visit(n: ExplicitConstructorInvocationStmt, context: outerCfg.Context): outerCfg.Context = {
            // TODO: explicit constructor
            log.warn("ExplicitConstructorInvocationStmt not supported")
            context
        }

        override def visit(n: ExpressionStmt, context: outerCfg.Context): outerCfg.Context = {
            n.getExpression.accept(exprVisitor, context.block)
            context
        }

        override def visit(n: ForEachStmt, context: outerCfg.Context): outerCfg.Context = {
            val ty = CodeUtil.typeOf(n.getVariable)
            context.block.seal()
            val entryBlock = outerCfg.createStatements()
            context.block.setNext(entryBlock)
            val iteExpr = n.getIterable.accept(exprVisitor, entryBlock)
            val tempIte = entryBlock.addStatement(IRMethodInvocation(outerCfg, "Iterable", "java.lang.Iterable.iterator()", iteExpr, Seq(), Set(n))).target
            val conditionBlock = outerCfg.createStatements()
            entryBlock.setNext(conditionBlock)
            entryBlock.seal()
            val condition = conditionBlock.addStatement(IRMethodInvocation(outerCfg, "boolean", "java.util.Iterator.hasNext()", Some(tempIte), Seq(), Set(n))).target
            val thenBlock = outerCfg.createStatements()
            ty.foreach(t => {
                val next = thenBlock.addStatement(IRMethodInvocation(outerCfg, t, "java.util.Iterator.next()", Some(tempIte), Seq(), Set(n))).target
                outerCfg.writeVar(n.getVariableDeclarator.getName.asString(), thenBlock, next)
            })
            val elseBlock = outerCfg.createStatements()
            val branch = outerCfg.createBranch(Some(condition), thenBlock, elseBlock)
            branch.seal()
            conditionBlock.setNext(branch)
            // TODO next
            val newContext = n.getBody.accept(this, context.push(thenBlock, elseBlock, entryBlock))
            newContext.block.seal()
            newContext.block.setNext(conditionBlock)
            conditionBlock.seal()
            newContext.pop(elseBlock)
        }

        override def visit(n: ForStmt, context: outerCfg.Context): outerCfg.Context = {
            context.block.seal()
            val entryBlock = outerCfg.createStatements()
            n.getInitialization.asScala.foreach(_.accept(exprVisitor, entryBlock))
            context.block.setNext(entryBlock)
            entryBlock.seal()
            val compareBlock = outerCfg.createStatements()
            entryBlock.setNext(compareBlock)
            val compareResult = n.getCompare.asScala.map(_.accept(exprVisitor, compareBlock))
            val thenBlock = outerCfg.createStatements()
            val elseBlock = outerCfg.createStatements()
            val updateBlock = outerCfg.createStatements()
            val branch = compareResult.map(r => outerCfg.createBranch(r, thenBlock, elseBlock))
            branch match {
                case Some(b) =>
                    compareBlock.setNext(b)
                    b.seal()
                case None =>
                    compareBlock.setNext(thenBlock)
            }
            val newContext = n.getBody.accept(this, context.push(thenBlock, elseBlock, updateBlock))
            newContext.block.seal()
            newContext.block.setNext(updateBlock)
            n.getUpdate.asScala.foreach(_.accept(exprVisitor, updateBlock))
            updateBlock.seal()
            updateBlock.setNext(compareBlock)
            compareBlock.seal()
            newContext.pop(elseBlock)
        }

        override def visit(n: IfStmt, context: outerCfg.Context): outerCfg.Context = {
            val condition = n.getCondition.accept(exprVisitor, context.block)
            val thenBlock = outerCfg.createStatements()
            val elseBlock = outerCfg.createStatements()
            val endBlock = outerCfg.createStatements()
            val branch = outerCfg.createBranch(condition, thenBlock, elseBlock)
            context.block.seal()
            context.block.setNext(branch)
            branch.seal()
            var newContext = n.getThenStmt.accept(this, context.push(thenBlock, endBlock, null))
            newContext.block.seal()
            newContext.block.setNext(endBlock)
            n.getElseStmt.asScala match {
                case Some(elseStmt) =>
                    newContext = elseStmt.accept(this, newContext.change(elseBlock))
                    newContext.block.seal()
                    newContext.block.setNext(endBlock)
                case None =>
                    elseBlock.seal()
                    elseBlock.setNext(endBlock)
            }
            newContext.pop(endBlock)
        }

        override def visit(n: LabeledStmt, context: outerCfg.Context): outerCfg.Context = {
            // TODO: label
            log.warn("Label of LabelStmt not supported")
            n.getStatement.accept(this, context)
        }

        override def visit(n: ReturnStmt, context: outerCfg.Context): outerCfg.Context = {
            for (expr <- n.getExpression.asScala.map(_.accept(exprVisitor, context.block))) context.block.addStatement(new IRReturn(expr, Set(n)))
            context
        }

        override def visit(n: SwitchStmt, context: outerCfg.Context): outerCfg.Context = {
            val c = for (selector <- n.getSelector.accept(exprVisitor, context.block)) yield {
                context.block.seal()
                val switch = outerCfg.createSwitch(selector)
                context.block.setNext(switch)
                val exitBlock = outerCfg.createStatements()
                // TODO: fall through
                n.getEntries.asScala.map(e => e.getLabels.asScala.toList -> e.getStatements.asScala).foreach {
                    case (Nil, s) =>
                        val statements = outerCfg.createStatements()
                        switch(DefaultLabel) = statements
                        val newContext = (context.push(statements, exitBlock, null) /: s) ((c, st) => st.accept(this, c))
                        newContext.block.seal()
                        newContext.block.setNext(exitBlock)
                    case (ls, s) =>
                        val labels = ls.flatMap(_.accept(exprVisitor, context.block).toList)
                        val statements = outerCfg.createStatements()
                        labels.map(ExpressionLabel.apply).foreach(switch(_) = statements)
                        val newContext = (context.push(statements, exitBlock, null) /: s) ((c, st) => st.accept(this, c))
                        newContext.block.seal()
                        newContext.block.setNext(exitBlock)
                }
                context.copy(block = exitBlock)
            }
            c.getOrElse(context)
        }

        override def visit(n: SynchronizedStmt, context: outerCfg.Context): outerCfg.Context = {
            n.getBody.accept(this, context)
        }

        override def visit(n: ThrowStmt, context: outerCfg.Context): outerCfg.Context = {
            for (exception <- n.getExpression.accept(exprVisitor, context.block)) context.block.addStatement(new IRThrow(exception, Set(n)))
            context
        }

        override def visit(n: TryStmt, context: outerCfg.Context): outerCfg.Context = {
            n.getResources.asScala.foreach(_.accept(exprVisitor, context.block))
            var newContext = n.getTryBlock.accept(this, context)
            log.warn("Catch clause not supported")
            //  TODO: catch clause
            //  newContext = (newContext /: cs) ((c, n) => visitCatchClause(cfg)(c, n))
            newContext = (newContext /: n.getFinallyBlock.asScala) ((c, n) => n.accept(this, c))
            newContext
        }

        override def visit(n: LocalClassDeclarationStmt, context: outerCfg.Context): outerCfg.Context = {
            // TODO: Local class decl
            log.warn("LocalClassDeclarationStmt not supported")
            context
        }

        override def visit(n: WhileStmt, context: outerCfg.Context): outerCfg.Context = {
            context.block.seal()
            val entryBlock = outerCfg.createStatements()
            context.block.setNext(entryBlock)
            val conditionBlock = outerCfg.createStatements()
            entryBlock.setNext(conditionBlock)
            val condition = n.getCondition.accept(exprVisitor, conditionBlock)
            val thenBlock = outerCfg.createStatements()
            val elseBlock = outerCfg.createStatements()
            val branch = outerCfg.createBranch(condition, thenBlock, elseBlock)
            conditionBlock.seal()
            branch.seal()
            conditionBlock.setNext(branch)
            val newContext = n.getBody.accept(this, context.push(thenBlock, elseBlock, entryBlock))
            newContext.block.seal()
            newContext.block.setNext(entryBlock)
            entryBlock.seal()
            newContext.pop(elseBlock)
        }

        override def defaultAction(n: Node, context: outerCfg.Context): outerCfg.Context = ???

        override def defaultAction(n: NodeList[_ <: Node], arg: outerCfg.Context): outerCfg.Context = ???
    }

}
