package com.github.woooking.cosyn.api.impl

import com.github.javaparser.ast.stmt._
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults
import com.github.javaparser.ast.{Node, NodeList}
import com.github.woooking.cosyn.dfgprocessor.cfg.{CFG, CFGImpl}

import scala.collection.JavaConverters._

class JavaStatementVisitor(val cfg: CFGImpl) extends GenericVisitorWithDefaults[CFGImpl#Context, CFGImpl#Context] {

    override def defaultAction(n: Node, arg: CFGImpl#Context): CFGImpl#Context = ???

    override def defaultAction(n: NodeList[_ <: Node], arg: CFGImpl#Context): CFGImpl#Context = ???

    override def visit(n: AssertStmt, arg: CFGImpl#Context): CFGImpl#Context = {

    }

    override def visit(blockStmt: BlockStmt, context: CFGImpl#Context): CFGImpl#Context = {
        (context /: blockStmt.getStatements.asScala) ((c, n) => n.accept(this, c))
    }

    override def visit(n: BreakStmt, arg: CFGImpl#Context): CFGImpl#Context = super.visit(n, arg)

    override def visit(n: ContinueStmt, arg: CFGImpl#Context): CFGImpl#Context = super.visit(n, arg)

    override def visit(n: DoStmt, arg: CFGImpl#Context): CFGImpl#Context = super.visit(n, arg)

    //    def visitStatement(cfg: CFGImpl)(context: cfg.Context, node: Statement): cfg.Context = node match {
    //        case AssertStmt(c, m) =>
    //            val check = visitExpression(cfg)(context.block, c)
    //            val message = m.map(visitExpression(cfg)(context.block, _))
    //            context.block.addStatement(new IRAssert(check, message, Set(node)))
    //            context
    //        case BreakStmt(_) =>
    //            context
    //        // TODO: change control flow
    //        case ContinueStmt(_) =>
    //            context
    //        // TODO: change control flow
    //        case DoStmt(body, compare) =>
    //            context.block.seal()
    //            val entryBlock = cfg.createStatements()
    //            val bodyBlock = cfg.createStatements()
    //            val exitBlock = cfg.createStatements()
    //            val compareBlock = cfg.createStatements()
    //            context.block.setNext(entryBlock)
    //            entryBlock.setNext(bodyBlock)
    //            val newContext = visitStatement(cfg)(cfg.createContext(bodyBlock, Some(exitBlock), Some(compareBlock)), body)
    //            newContext.block.seal()
    //            newContext.block.setNext(compareBlock)
    //            val compareResult = visitExpression(cfg)(compareBlock, compare)
    //            compareBlock.seal()
    //            val branch = cfg.createBranch(compareResult, bodyBlock, exitBlock)
    //            compareBlock.setNext(branch)
    //            branch.seal()
    //            entryBlock.seal()
    //            cfg.createContext(exitBlock)
    //        case EmptyStmt() =>
    //            context
    //        case ExpressionStmt(expression) =>
    //            visitExpression(cfg)(context.block, expression)
    //            context
    //        case ExplicitConstructorInvocationStmt(_, None, _, _) =>
    //            // TODO: explicit constructor
    //            context
    //        case ExplicitConstructorInvocationStmt(_, Some(_), _, _) =>
    //            // TODO: explicit constructor
    //            context
    //        case ForStmt(init, None, update, body) =>
    //            context.block.seal()
    //            val entryBlock = cfg.createStatements()
    //            init.foreach(visitExpression(cfg)(entryBlock, _))
    //            context.block.setNext(entryBlock)
    //            entryBlock.seal()
    //            val compareBlock = cfg.createStatements()
    //            entryBlock.setNext(compareBlock)
    //            val thenBlock = cfg.createStatements()
    //            val elseBlock = cfg.createStatements()
    //            val updateBlock = cfg.createStatements()
    //            compareBlock.setNext(thenBlock)
    //            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(updateBlock)), body)
    //            newContext.block.seal()
    //            newContext.block.setNext(updateBlock)
    //            update.foreach(visitExpression(cfg)(updateBlock, _))
    //            updateBlock.seal()
    //            updateBlock.setNext(compareBlock)
    //            compareBlock.seal()
    //            cfg.createContext(elseBlock)
    //        case ForStmt(init, Some(compare), update, body) =>
    //            context.block.seal()
    //            val entryBlock = cfg.createStatements()
    //            init.foreach(visitExpression(cfg)(entryBlock, _))
    //            context.block.setNext(entryBlock)
    //            entryBlock.seal()
    //            val compareBlock = cfg.createStatements()
    //            entryBlock.setNext(compareBlock)
    //            val compareResult = visitExpression(cfg)(compareBlock, compare)
    //            val thenBlock = cfg.createStatements()
    //            val elseBlock = cfg.createStatements()
    //            val updateBlock = cfg.createStatements()
    //            val branch = cfg.createBranch(compareResult, thenBlock, elseBlock)
    //            compareBlock.setNext(branch)
    //            branch.seal()
    //            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(updateBlock)), body)
    //            newContext.block.seal()
    //            newContext.block.setNext(updateBlock)
    //            update.foreach(visitExpression(cfg)(updateBlock, _))
    //            updateBlock.seal()
    //            updateBlock.setNext(compareBlock)
    //            compareBlock.seal()
    //            cfg.createContext(elseBlock)
    //        case ForeachStmt(v, ite, b) =>
    //            context.block.seal()
    //            val entryBlock = cfg.createStatements()
    //            context.block.setNext(entryBlock)
    //            val iteExpr = visitExpression(cfg)(entryBlock, ite)
    //            val tempIte = entryBlock.addStatement(IRMethodInvocation(cfg, "iterator", Some(iteExpr), Seq(), Set(node))).target
    //            val conditionBlock = cfg.createStatements()
    //            entryBlock.setNext(conditionBlock)
    //            entryBlock.seal()
    //            val condition = conditionBlock.addStatement(IRMethodInvocation(cfg, "hasNext", Some(tempIte), Seq(), Set(node))).target
    //            val thenBlock = cfg.createStatements()
    //            val next = thenBlock.addStatement(IRMethodInvocation(cfg, "next", Some(tempIte), Seq(), Set(node))).target
    //            cfg.writeVar(v.getVariables.get(0).getName.asString(), thenBlock, next)
    //            val elseBlock = cfg.createStatements()
    //            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
    //            branch.seal()
    //            conditionBlock.setNext(branch)
    //            // TODO next
    //            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)), b)
    //            newContext.block.seal()
    //            newContext.block.setNext(conditionBlock)
    //            conditionBlock.seal()
    //            cfg.createContext(elseBlock)
    //        case IfStmt(c, t, None) =>
    //            val condition = visitExpression(cfg)(context.block, c)
    //            val thenBlock = cfg.createStatements()
    //            val endBlock = cfg.createStatements()
    //            val branch = cfg.createBranch(condition, thenBlock, endBlock)
    //            context.block.seal()
    //            context.block.setNext(branch)
    //            branch.seal()
    //            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock), t)
    //            newContext.block.seal()
    //            newContext.block.setNext(endBlock)
    //            cfg.createContext(endBlock)
    //        case IfStmt(c, t, Some(e)) =>
    //            val condition = visitExpression(cfg)(context.block, c)
    //            val thenBlock = cfg.createStatements()
    //            val elseBlock = cfg.createStatements()
    //            val endBlock = cfg.createStatements()
    //            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
    //            context.block.seal()
    //            context.block.setNext(branch)
    //            branch.seal()
    //            var newContext = visitStatement(cfg)(cfg.createContext(thenBlock), t)
    //            newContext.block.seal()
    //            newContext.block.setNext(endBlock)
    //            newContext = visitStatement(cfg)(cfg.createContext(elseBlock), e)
    //            newContext.block.seal()
    //            newContext.block.setNext(endBlock)
    //            cfg.createContext(endBlock)
    //        case LabeledStmt(_, s) =>
    //            // TODO: label
    //            visitStatement(cfg)(context, s)
    //        case LocalClassDeclarationStmt(_) =>
    //            // TODO: Local class decl
    //            context
    //        case ReturnStmt(expression) =>
    //            val expr = expression.map(node => visitExpression(cfg)(context.block, node))
    //            context.block.addStatement(new IRReturn(expr, Set(node)))
    //            context
    //        case SwitchStmt(s, es) =>
    //            val selector = visitExpression(cfg)(context.block, s)
    //            context.block.seal()
    //            val switch = cfg.createSwitch(selector)
    //            context.block.setNext(switch)
    //            val exitBlock = cfg.createStatements()
    //            // TODO: fall through
    //            es.foreach {
    //                case SwitchEntryStmt(Some(l), s) =>
    //                    val label = visitExpression(cfg)(context.block, l)
    //                    val statements = cfg.createStatements()
    //                    switch(ExpressionLabel(label)) = statements
    //                    val newContext = (cfg.createContext(statements, Some(exitBlock), None) /: s) ((c, st) => visitStatement(cfg)(c, st))
    //                    newContext.block.seal()
    //                    newContext.block.setNext(exitBlock)
    //                case SwitchEntryStmt(None, s) =>
    //                    val statements = cfg.createStatements()
    //                    switch(DefaultLabel) = statements
    //                    val newContext = (cfg.createContext(statements, Some(exitBlock), None) /: s) ((c, st) => visitStatement(cfg)(c, st))
    //                    newContext.block.seal()
    //                    newContext.block.setNext(exitBlock)
    //            }
    //            context.copy(block = exitBlock)
    //        case SynchronizedStmt(_, b) =>
    //            visitStatement(cfg)(context, b)
    //        case ThrowStmt(e) =>
    //            val exception = visitExpression(cfg)(context.block, e)
    //            context.block.addStatement(new IRThrow(exception, Set(node)))
    //            context
    //        case TryStmt(r, t, _, f) =>
    //            // TODO
    //            r.foreach(visitExpression(cfg)(context.block, _))
    //            var newContext = visitStatement(cfg)(context, t)
    //            //            newContext = (newContext /: cs) ((c, n) => visitCatchClause(cfg)(c, n))
    //            newContext = (newContext /: f) ((c, n) => visitStatement(cfg)(c, n))
    //            newContext
    //        case WhileStmt(c, b) =>
    //            context.block.seal()
    //            val entryBlock = cfg.createStatements()
    //            context.block.setNext(entryBlock)
    //            val conditionBlock = cfg.createStatements()
    //            entryBlock.setNext(conditionBlock)
    //            val condition = visitExpression(cfg)(conditionBlock, c)
    //            val thenBlock = cfg.createStatements()
    //            val elseBlock = cfg.createStatements()
    //            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
    //            conditionBlock.seal()
    //            branch.seal()
    //            conditionBlock.setNext(branch)
    //            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)), b)
    //            newContext.block.seal()
    //            newContext.block.setNext(entryBlock)
    //            entryBlock.seal()
    //            cfg.createContext(elseBlock)
    //    }
}
