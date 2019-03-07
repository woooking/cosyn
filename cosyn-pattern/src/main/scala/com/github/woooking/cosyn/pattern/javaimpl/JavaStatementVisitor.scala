package com.github.woooking.cosyn.pattern.javaimpl

import com.github.javaparser.ast.stmt._
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults
import com.github.javaparser.ast.{Node, NodeList}
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFGSwitch.{DefaultLabel, ExpressionLabel}
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements.{IRAssert, IRMethodInvocation, IRReturn, IRThrow}
import com.github.woooking.cosyn.pattern.util.OptionConverters._
import org.slf4s.Logging

import scala.collection.JavaConverters._

class JavaStatementVisitor(private val cfg: CFG) extends GenericVisitorWithDefaults[CFG#Context, CFG#Context] with Logging {
    private val exprVisitor = new JavaExpressionVisitor(cfg)

    override def defaultAction(n: Node, context: CFG#Context): CFG#Context = ???

    override def defaultAction(n: NodeList[_ <: Node], arg: CFG#Context): CFG#Context = ???

    override def visit(n: AssertStmt, context: CFG#Context): CFG#Context = {
        val check = n.getCheck.accept(exprVisitor, context.block)
        val message = n.getMessage.asScala.map(_.accept(exprVisitor, context.block))
        context.block.addStatement(new IRAssert(check, message, Set(n)))
        context
    }

    override def visit(blockStmt: BlockStmt, context: CFG#Context): CFG#Context = {
        (context /: blockStmt.getStatements.asScala) ((c, n) => n.accept(this, c))
    }

    override def visit(n: BreakStmt, context: CFG#Context): CFG#Context = {
        // TODO: change control flow
        log.warn("BreakStmt needs to be enhanced")
        context
    }

    override def visit(n: ContinueStmt, context: CFG#Context): CFG#Context = {
        // TODO: change control flow
        log.warn("ContinueStmt needs to be enhanced")
        context
    }

    override def visit(n: DoStmt, context: CFG#Context): CFG#Context = {
        context.block.seal()
        val entryBlock = cfg.createStatements()
        val bodyBlock = cfg.createStatements()
        val exitBlock = cfg.createStatements()
        val compareBlock = cfg.createStatements()
        context.block.setNext(entryBlock)
        entryBlock.setNext(bodyBlock)
        val newContext = n.getBody.accept(this, cfg.createContext(bodyBlock, Some(exitBlock), Some(compareBlock)))
        newContext.block.seal()
        newContext.block.setNext(compareBlock)
        val compareResult = n.getCondition.accept(exprVisitor, compareBlock)
        compareBlock.seal()
        val branch = cfg.createBranch(compareResult, bodyBlock, exitBlock)
        compareBlock.setNext(branch)
        branch.seal()
        entryBlock.seal()
        cfg.createContext(exitBlock)
    }

    override def visit(n: EmptyStmt, context: CFG#Context): CFG#Context = {
        context
    }

    override def visit(n: ExplicitConstructorInvocationStmt, context: CFG#Context): CFG#Context = {
        // TODO: explicit constructor
        log.warn("ExplicitConstructorInvocationStmt not supported")
        context
    }

    override def visit(n: ExpressionStmt, context: CFG#Context): CFG#Context = {
        n.getExpression.accept(exprVisitor, context.block)
        context
    }

    override def visit(n: ForEachStmt, context: CFG#Context): CFG#Context = {
        context.block.seal()
        val entryBlock = cfg.createStatements()
        context.block.setNext(entryBlock)
        val iteExpr = n.getIterable.accept(exprVisitor, entryBlock)
        val tempIte = entryBlock.addStatement(IRMethodInvocation(cfg, "java.lang.Iterable.iterator()", Some(iteExpr), Seq(), Set(n))).target
        val conditionBlock = cfg.createStatements()
        entryBlock.setNext(conditionBlock)
        entryBlock.seal()
        val condition = conditionBlock.addStatement(IRMethodInvocation(cfg, "java.util.Iterator.hasNext()", Some(tempIte), Seq(), Set(n))).target
        val thenBlock = cfg.createStatements()
        val next = thenBlock.addStatement(IRMethodInvocation(cfg, "java.util.Iterator.next()", Some(tempIte), Seq(), Set(n))).target
        cfg.writeVar(n.getVariableDeclarator.getName.asString(), thenBlock, next)
        val elseBlock = cfg.createStatements()
        val branch = cfg.createBranch(condition, thenBlock, elseBlock)
        branch.seal()
        conditionBlock.setNext(branch)
        // TODO next
        val newContext = n.getBody.accept(this, cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)))
        newContext.block.seal()
        newContext.block.setNext(conditionBlock)
        conditionBlock.seal()
        cfg.createContext(elseBlock)
    }

    override def visit(n: ForStmt, context: CFG#Context): CFG#Context = {
        context.block.seal()
        val entryBlock = cfg.createStatements()
        n.getInitialization.asScala.foreach(_.accept(exprVisitor, entryBlock))
        context.block.setNext(entryBlock)
        entryBlock.seal()
        val compareBlock = cfg.createStatements()
        entryBlock.setNext(compareBlock)
        val compareResult = n.getCompare.asScala.map(_.accept(exprVisitor, compareBlock))
        val thenBlock = cfg.createStatements()
        val elseBlock = cfg.createStatements()
        val updateBlock = cfg.createStatements()
        val branch = compareResult.map(r => cfg.createBranch(r, thenBlock, elseBlock))
        branch match {
            case Some(b) =>
                compareBlock.setNext(b)
                b.seal()
            case None =>
                compareBlock.setNext(thenBlock)
        }
        val newContext = n.getBody.accept(this, cfg.createContext(thenBlock, Some(elseBlock), Some(updateBlock)))
        newContext.block.seal()
        newContext.block.setNext(updateBlock)
        n.getUpdate.asScala.foreach(_.accept(exprVisitor, updateBlock))
        updateBlock.seal()
        updateBlock.setNext(compareBlock)
        compareBlock.seal()
        cfg.createContext(elseBlock)
    }

    override def visit(n: IfStmt, context: CFG#Context): CFG#Context = {
        val condition = n.getCondition.accept(exprVisitor, context.block)
        val thenBlock = cfg.createStatements()
        val elseBlock = cfg.createStatements()
        val endBlock = cfg.createStatements()
        val branch = cfg.createBranch(condition, thenBlock, elseBlock)
        context.block.seal()
        context.block.setNext(branch)
        branch.seal()
        var newContext = n.getThenStmt.accept(this, cfg.createContext(thenBlock))
        newContext.block.seal()
        newContext.block.setNext(endBlock)
        n.getElseStmt.asScala match {
            case Some(elseStmt) =>
                newContext = elseStmt.accept(this, cfg.createContext(elseBlock))
                newContext.block.seal()
                newContext.block.setNext(endBlock)
            case None =>
                elseBlock.seal()
                elseBlock.setNext(endBlock)
        }
        cfg.createContext(endBlock)
    }

    override def visit(n: LabeledStmt, context: CFG#Context): CFG#Context = {
        // TODO: label
        log.warn("Label of LabelStmt not supported")
        n.getStatement.accept(this, context)
    }

    override def visit(n: ReturnStmt, context: CFG#Context): CFG#Context = {
        val expr = n.getExpression.asScala.map(_.accept(exprVisitor, context.block))
        context.block.addStatement(new IRReturn(expr, Set(n)))
        context
    }

    override def visit(n: SwitchStmt, context: CFG#Context): CFG#Context = {
        val selector = n.getSelector.accept(exprVisitor, context.block)
        context.block.seal()
        val switch = cfg.createSwitch(selector)
        context.block.setNext(switch)
        val exitBlock = cfg.createStatements()
        // TODO: fall through
        n.getEntries.asScala.map(e => e.getLabels.asScala.toList -> e.getStatements.asScala).foreach {
            case (Nil, s) =>
                val statements = cfg.createStatements()
                switch(DefaultLabel) = statements
                val newContext = (cfg.createContext(statements, Some(exitBlock), None).asInstanceOf[CFG#Context] /: s) ((c, st) => st.accept(this, c))
                newContext.block.seal()
                newContext.block.setNext(exitBlock)
            case (ls, s) =>
                val labels = ls.map(_.accept(exprVisitor, context.block))
                val statements = cfg.createStatements()
                labels.map(ExpressionLabel.apply).foreach(switch(_) = statements)
                val newContext = (cfg.createContext(statements, Some(exitBlock), None).asInstanceOf[CFG#Context] /: s) ((c, st) => st.accept(this, c))
                newContext.block.seal()
                newContext.block.setNext(exitBlock)
        }
        context.copy(block = exitBlock)
    }

    override def visit(n: SynchronizedStmt, context: CFG#Context): CFG#Context = {
        n.getBody.accept(this, context)
    }

    override def visit(n: ThrowStmt, context: CFG#Context): CFG#Context = {
        val exception = n.getExpression.accept(exprVisitor, context.block)
        context.block.addStatement(new IRThrow(exception, Set(n)))
        context
    }

    override def visit(n: TryStmt, context: CFG#Context): CFG#Context = {
        n.getResources.asScala.foreach(_.accept(exprVisitor, context.block))
        var newContext = n.getTryBlock.accept(this, context)
        log.warn("Catch clause not supported")
        //  TODO: catch clause
        //  newContext = (newContext /: cs) ((c, n) => visitCatchClause(cfg)(c, n))
        newContext = (newContext /: n.getFinallyBlock.asScala) ((c, n) => n.accept(this, c))
        newContext
    }

    override def visit(n: LocalClassDeclarationStmt, context: CFG#Context): CFG#Context = {
        // TODO: Local class decl
        log.warn("LocalClassDeclarationStmt not supported")
        context
    }

    override def visit(n: WhileStmt, context: CFG#Context): CFG#Context = {
        context.block.seal()
        val entryBlock = cfg.createStatements()
        context.block.setNext(entryBlock)
        val conditionBlock = cfg.createStatements()
        entryBlock.setNext(conditionBlock)
        val condition = n.getCondition.accept(exprVisitor, conditionBlock)
        val thenBlock = cfg.createStatements()
        val elseBlock = cfg.createStatements()
        val branch = cfg.createBranch(condition, thenBlock, elseBlock)
        conditionBlock.seal()
        branch.seal()
        conditionBlock.setNext(branch)
        val newContext = n.getBody.accept(this, cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)))
        newContext.block.seal()
        newContext.block.setNext(entryBlock)
        entryBlock.seal()
        cfg.createContext(elseBlock)
    }
}
