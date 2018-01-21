package com.github.woooking.cosyn.ir

import com.github.javaparser.ast.stmt.CatchClause
import com.github.woooking.cosyn.ProjectParser
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.statements._
import com.github.woooking.cosyn.javaparser.CompilationUnit
import com.github.woooking.cosyn.javaparser.body._
import com.github.woooking.cosyn.javaparser.expr._
import com.github.woooking.cosyn.javaparser.stmt._

import scala.annotation.tailrec

class Visitor(parser: ProjectParser) {
    type CFGMap = Map[String, CFG]

    def generateCFGs(cu: CompilationUnit): CFGMap = generateCFGs(cu.types, Map.empty)

    @tailrec
    private def generateCFGs(bodyDecls: List[BodyDeclaration[_]], methods: CFGMap): CFGMap = bodyDecls match {
        case Nil => methods
        case body :: rest => generateCFGs(rest, methods ++ generateCFGs(body))
    }

    private def generateCFGs(bodyDecl: BodyDeclaration[_]): CFGMap = bodyDecl match {
        case ClassOrInterfaceDeclaration(_, _, _, _, typeDecls) => generateCFGs(typeDecls, Map.empty)
        case FieldDeclaration(_) => Map.empty
        case InitializerDeclaration(_, _) => Map.empty
        case d @ MethodDeclaration(_, _, _, _, _, _, Some(_)) =>
            val cfg = generateCFG(d)
            Map(d.signature -> cfg)
        case d @ ConstructorDeclaration(_, _, _, _, _, _) =>
            val cfg = generateCFG(d)
            Map(d.signature -> cfg)
    }

    def generateCFG(decl: ConstructorDeclaration): CFG = {
        val cfg = new CFG
        decl.params.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, p.getType)))
        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl.body)
        pair.block.seal()
        pair.block.setNext(cfg.exit)
        cfg.optimize()
        cfg
    }

    def generateCFG(decl: MethodDeclaration): CFG = {
        val cfg = new CFG
        decl.params.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, p.getType)))
        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl.body.get)
        pair.block.seal()
        pair.block.setNext(cfg.exit)
        cfg.optimize()
        cfg
    }


    //
    //    def f(node: NodeDelegate[_]): Unit = node match {
    //        case CompilationUnit(_, _, _, types) => f(types)
    // - Body ----------------------------------------------
    //        case ClassOrInterfaceDeclaration(_, _, _, _, members) => f(members)
    //        case FieldDeclaration(_) =>
    //        case ConstructorDeclaration(name, _, params, _, _, body) =>
    //            val cfg = new CFG
    //            params.asScala.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, p.getType)))
    //            val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), body)
    //            methods(name.getIdentifier) = cfg
    //            pair.block.seal()
    //            pair.block.setNext(cfg.exit)
    //
    //            println("=====")
    //            println(name)
    //            cfg.print()
    //        case MethodDeclaration(name, _, _, params, _, _, Some(body)) =>
    //            val cfg = new CFG
    //            params.asScala.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, p.getType)))
    //            val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), body)
    //            methods(name.getIdentifier) = cfg
    //            pair.block.seal()
    //            pair.block.setNext(cfg.exit)
    //
    //            println("=====")
    //            println(name)
    //            cfg.print()
    //        case nodeList: NodeList[_] => nodeList.asScala.map(f)
    //        case _ =>
    //            println(node.getClass)
    //            ???
    //    }

    def visitVariableDeclarator(cfg: CFG)(block: cfg.Statements, node: VariableDeclarator): IRExpression = node match {
        case VariableDeclarator(name, _, initializer) =>
            val temp = cfg.createTempVar()
            cfg.writeVar(name, block, temp)
            initializer.map((node: Expression[_]) => visitExpression(cfg)(block, node)).foreach(i => block.addStatement(IRAssignment(temp, i)))
            temp
    }

    def visitCatchClause(cfg: CFG)(block: cfg.Context, node: CatchClause): cfg.Context = ???

    def visitStatement(cfg: CFG)(context: cfg.Context, node: Statement): cfg.Context = node match {
        case BlockStmt(statements) =>
            (context /: statements) ((c, n) => visitStatement(cfg)(c, n))
        case ExpressionStmt(expression) =>
            visitExpression(cfg)(context.block, expression)
            context
        case BreakStmt(None) =>
            context
        // TODO: change control flow
        case ReturnStmt(expression) =>
            val expr = expression.map(node => visitExpression(cfg)(context.block, node))
            context.block.addStatement(IRReturn(expr))
            context
        // TODO: change control flow
        case IfStmt(c, t, None) =>
            val condition = visitExpression(cfg)(context.block, c)
            val thenBlock = cfg.createStatements()
            val endBlock = cfg.createStatements()
            val branch = cfg.createBranch(condition, thenBlock, endBlock)
            context.block.seal()
            context.block.setNext(branch)
            branch.seal()
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock), t)
            newContext.block.seal()
            newContext.block.setNext(endBlock)
            cfg.createContext(endBlock)
        case IfStmt(c, t, Some(e)) =>
            val condition = visitExpression(cfg)(context.block, c)
            val thenBlock = cfg.createStatements()
            val elseBlock = cfg.createStatements()
            val endBlock = cfg.createStatements()
            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
            context.block.seal()
            context.block.setNext(branch)
            branch.seal()
            var newContext = visitStatement(cfg)(cfg.createContext(thenBlock), t)
            newContext.block.seal()
            newContext.block.setNext(endBlock)
            newContext = visitStatement(cfg)(cfg.createContext(elseBlock), e)
            newContext.block.seal()
            newContext.block.setNext(endBlock)
            cfg.createContext(endBlock)
        case WhileStmt(c, b) =>
            context.block.seal()
            val entryBlock = cfg.createStatements()
            context.block.setNext(entryBlock)
            val conditionBlock = cfg.createStatements()
            entryBlock.setNext(conditionBlock)
            val condition = visitExpression(cfg)(conditionBlock, c)
            val thenBlock = cfg.createStatements()
            val elseBlock = cfg.createStatements()
            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
            conditionBlock.seal()
            branch.seal()
            conditionBlock.setNext(branch)
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)), b)
            newContext.block.seal()
            newContext.block.setNext(entryBlock)
            entryBlock.seal()
            cfg.createContext(elseBlock)
        case ContinueStmt(None) =>
            context
        // TODO: change control flow
        case ForStmt(init, Some(compare), update, body) =>
            context.block.seal()
            val entryBlock = cfg.createStatements()
            init.foreach(visitExpression(cfg)(entryBlock, _))
            context.block.setNext(entryBlock)
            entryBlock.seal()
            val compareBlock = cfg.createStatements()
            entryBlock.setNext(compareBlock)
            val compareResult = visitExpression(cfg)(compareBlock, compare)
            val thenBlock = cfg.createStatements()
            val elseBlock = cfg.createStatements()
            val updateBlock = cfg.createStatements()
            val branch = cfg.createBranch(compareResult, thenBlock, elseBlock)
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(updateBlock)), body)
            newContext.block.seal()
            newContext.block.setNext(updateBlock)
            update.foreach(visitExpression(cfg)(updateBlock, _))
            updateBlock.seal()
            updateBlock.setNext(compareBlock)
            compareBlock.seal()
            cfg.createContext(elseBlock)
        case ForeachStmt(_, ite, b) =>
            context.block.seal()
            val entryBlock = cfg.createStatements()
            context.block.setNext(entryBlock)
            val iteExpr = visitExpression(cfg)(entryBlock, ite)
            val tempIte = cfg.createTempVar()
            entryBlock.addStatement(IRMethodInvocation(tempIte, "iterator", Some(iteExpr), Seq()))
            val conditionBlock = cfg.createStatements()
            entryBlock.setNext(conditionBlock)
            val condition = cfg.createTempVar()
            conditionBlock.addStatement(IRMethodInvocation(condition, "hasNext", Some(iteExpr), Seq()))
            val thenBlock = cfg.createStatements()
            val elseBlock = cfg.createStatements()
            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
            conditionBlock.seal()
            branch.seal()
            conditionBlock.setNext(branch)
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)), b)
            newContext.block.seal()
            newContext.block.setNext(entryBlock)
            entryBlock.seal()
            cfg.createContext(elseBlock)
        case TryStmt(r, t, _, f) =>
            // TODO
            r.foreach(visitExpression(cfg)(context.block, _))
            var newContext = visitStatement(cfg)(context, t)
            //            newContext = (newContext /: cs) ((c, n) => visitCatchClause(cfg)(c, n))
            newContext = (newContext /: f) ((c, n) => visitStatement(cfg)(c, n))
            newContext
    }


    def visitExpression(cfg: CFG)(block: cfg.Statements, node: Expression[_]): IRExpression = node match {
        case AssignExpr(t, ope, s) =>
            // TODO: left hand side
            val target = visitExpression(cfg)(block, t)
            val source = visitExpression(cfg)(block, s)
            ope match {
                case AssignExpr.Operator.Assign =>
                    block.addStatement(IRAssignment(target, source))
                case _ =>
                    val temp = cfg.createTempVar()
                    block.addStatement(IRBinaryOperation(temp, BinaryOperator.fromAssignExprOperator(ope), target, source))
            }
            target
        case BinaryExpr(l, ope, r) =>
            val lhs = visitExpression(cfg)(block, l)
            val rhs = visitExpression(cfg)(block, r)
            val temp = cfg.createTempVar()
            block.addStatement(IRBinaryOperation(temp, BinaryOperator.fromBinaryExprOperator(ope), lhs, rhs))
            temp
        case CastExpr(_, e) =>
            visitExpression(cfg)(block, e)
        case FieldAccessExpr(scope, name, _) =>
            val receiver = visitExpression(cfg)(block, scope)
            val temp = cfg.createTempVar()
            block.addStatement(IRFieldAccess(temp, receiver, name))
            temp
        case StringLiteralExpr(n) => IRString(n)
        case CharLiteralExpr(n) => IRChar(n)
        case IntegerLiteralExpr(n) => IRInteger(n)
        case BooleanLiteralExpr(true) => IRExpression.True
        case BooleanLiteralExpr(false) => IRExpression.False
        case NullLiteralExpr() => IRNull
        case n @ MethodCallExpr(_, scope, _, arguments) =>
            val receiver = scope.map(node => visitExpression(cfg)(block, node))
            val args = arguments.map(node => visitExpression(cfg)(block, node))
            val temp = cfg.createTempVar()
            block.addStatement(IRMethodInvocation(temp, n.delegate.getName.getIdentifier, receiver, args))
            temp
        case NameExpr(name) =>
            cfg.readVar(name, block) match {
                case IRUndef => IRExtern(name)
                case e => e
            }
        case ObjectCreationExpr(_, ty, _, arguments, _) =>
            val args = arguments.map(node => visitExpression(cfg)(block, node))
            val temp = cfg.createTempVar()
            block.addStatement(IRMethodInvocation(temp, "<init>", Some(IRTypeObject(ty)), args))
            temp
        //        case ThisExpr(_) =>
        //            // TODO: more specific
        //            IRThis
        //        case SuperExpr(expr) =>
        //            // TODO: more specific
        //            val receiver = expr.map((node: Expression) => visitExpression(cfg)(block, node)).getOrElse(IRThis)
        //            val temp = cfg.createTempVar()
        //            block.addStatement(IRFieldAccess(temp, receiver, "super"))
        //            temp
        case UnaryExpr(ope, e) =>
            val temp = cfg.createTempVar()
            val source = visitExpression(cfg)(block, e)
            block.addStatement(IRUnaryOperation(temp, UnaryOperator.fromAssignExprOperator(ope), source))
            temp
        case VariableDeclarationExpr(declarators) =>
            declarators.map(visitVariableDeclarator(cfg)(block, _))
            null // TODO
    }

}
