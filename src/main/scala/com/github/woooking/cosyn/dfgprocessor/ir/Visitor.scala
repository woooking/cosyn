package com.github.woooking.cosyn.ir

import com.github.javaparser.ast.stmt.CatchClause
import com.github.javaparser.ast.expr.{UnaryExpr => JPUnaryExpr}
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGSwitch.{DefaultLabel, ExpressionLabel}
import com.github.woooking.cosyn.dfgprocessor.cfg.{CFG, CFGStatements}
import com.github.woooking.cosyn.ir.statements._
import com.github.woooking.cosyn.javaparser.CompilationUnit
import com.github.woooking.cosyn.javaparser.body._
import com.github.woooking.cosyn.javaparser.expr._
import com.github.woooking.cosyn.javaparser.stmt._

import scala.annotation.tailrec

object Visitor {
    def generateCFGs(cu: CompilationUnit): Seq[CFG] = generateCFGs(cu.file, cu.types, Seq.empty)

    @tailrec
    private def generateCFGs(file: String, bodyDecls: List[BodyDeclaration[_]], methods: Seq[CFG]): Seq[CFG] = bodyDecls match {
        case Nil => methods
        case body :: rest => generateCFGs(file, rest, methods ++ generateCFGs(file, body))
    }

    private def generateCFGs(file: String, bodyDecl: BodyDeclaration[_]): Seq[CFG] = bodyDecl match {
        case AnnotationDeclaration(_, _) => Seq.empty
        case ClassOrInterfaceDeclaration(_, _, _, _, typeDecls) => generateCFGs(file, typeDecls, Seq.empty)
        case FieldDeclaration(_) => Seq.empty
        case InitializerDeclaration(_, _) => Seq.empty
        case d @ MethodDeclaration(_, _, _, _, _, _, Some(_)) =>
            val cfg = generateCFG(file, d)
            Seq(cfg)
        case MethodDeclaration(_, _, _, _, _, _, None) =>
            Seq.empty
        case d @ ConstructorDeclaration(_, _, _, _, _, _) =>
            val cfg = generateCFG(file, d)
            Seq(cfg)
        case EnumDeclaration(_, _, _, _) =>
            // TODO: enum
            Seq.empty
    }

    def generateCFG(file: String, decl: ConstructorDeclaration): CFG = {
        val cfg = new CFG(file, decl.name, decl)
        decl.params.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, p.getType)))
        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl.body)
        pair.block.seal()
        pair.block.setNext(cfg.exit)
        cfg.optimize()
        cfg
    }

    def generateCFG(file: String, decl: MethodDeclaration): CFG = {
        val cfg = new CFG(file, decl.name, decl)
        decl.params.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, p.getType)))
        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl.body.get)
        pair.block.seal()
        pair.block.setNext(cfg.exit)
        cfg.optimize()
        cfg
    }

    def visitVariableDeclarator(cfg: CFG)(block: CFGStatements, node: VariableDeclarator): IRExpression = node match {
        case VariableDeclarator(name, _, initializer) =>
            val initValue = initializer.map(node => visitExpression(cfg)(block, node)).getOrElse(IRNull)
            initValue match {
                case t: IRTemp =>
                    cfg.writeVar(name, block, t)
                    t
                case _ =>
                    val target = block.addStatement(new IRAssignment(cfg, initValue, Set(node))).target
                    cfg.writeVar(name, block, target)
                    target
            }
    }

    def visitCatchClause(cfg: CFG)(block: cfg.Context, node: CatchClause): cfg.Context = ???

    def visitStatement(cfg: CFG)(context: cfg.Context, node: Statement): cfg.Context = node match {
        case AssertStmt(c, m) =>
            val check = visitExpression(cfg)(context.block, c)
            val message = m.map(visitExpression(cfg)(context.block, _))
            context.block.addStatement(new IRAssert(check, message, Set(node)))
            context
        case BlockStmt(statements) =>
            (context /: statements) ((c, n) => visitStatement(cfg)(c, n))
        case BreakStmt(_) =>
            context
        // TODO: change control flow
        case ContinueStmt(_) =>
            context
        // TODO: change control flow
        case DoStmt(body, compare) =>
            context.block.seal()
            val entryBlock = cfg.createStatements()
            val bodyBlock = cfg.createStatements()
            val exitBlock = cfg.createStatements()
            val compareBlock = cfg.createStatements()
            context.block.setNext(entryBlock)
            entryBlock.setNext(bodyBlock)
            val newContext = visitStatement(cfg)(cfg.createContext(bodyBlock, Some(exitBlock), Some(compareBlock)), body)
            newContext.block.seal()
            newContext.block.setNext(compareBlock)
            val compareResult = visitExpression(cfg)(compareBlock, compare)
            compareBlock.seal()
            val branch = cfg.createBranch(compareResult, bodyBlock, exitBlock)
            compareBlock.setNext(branch)
            branch.seal()
            entryBlock.seal()
            cfg.createContext(exitBlock)
        case EmptyStmt() =>
            context
        case ExpressionStmt(expression) =>
            visitExpression(cfg)(context.block, expression)
            context
        case ExplicitConstructorInvocationStmt(_, None, _, _) =>
            // TODO: explicit constructor
            context
        case ExplicitConstructorInvocationStmt(_, Some(_), _, _) =>
            // TODO: explicit constructor
            context
        case ForStmt(init, None, update, body) =>
            context.block.seal()
            val entryBlock = cfg.createStatements()
            init.foreach(visitExpression(cfg)(entryBlock, _))
            context.block.setNext(entryBlock)
            entryBlock.seal()
            val compareBlock = cfg.createStatements()
            entryBlock.setNext(compareBlock)
            val thenBlock = cfg.createStatements()
            val elseBlock = cfg.createStatements()
            val updateBlock = cfg.createStatements()
            compareBlock.setNext(thenBlock)
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(updateBlock)), body)
            newContext.block.seal()
            newContext.block.setNext(updateBlock)
            update.foreach(visitExpression(cfg)(updateBlock, _))
            updateBlock.seal()
            updateBlock.setNext(compareBlock)
            compareBlock.seal()
            cfg.createContext(elseBlock)
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
            compareBlock.setNext(branch)
            branch.seal()
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(updateBlock)), body)
            newContext.block.seal()
            newContext.block.setNext(updateBlock)
            update.foreach(visitExpression(cfg)(updateBlock, _))
            updateBlock.seal()
            updateBlock.setNext(compareBlock)
            compareBlock.seal()
            cfg.createContext(elseBlock)
        case ForeachStmt(v, ite, b) =>
            context.block.seal()
            val entryBlock = cfg.createStatements()
            context.block.setNext(entryBlock)
            val iteExpr = visitExpression(cfg)(entryBlock, ite)
            val tempIte = entryBlock.addStatement(new IRMethodInvocation(cfg, "iterator", Some(iteExpr), Seq(), Set(node))).target
            val conditionBlock = cfg.createStatements()
            entryBlock.setNext(conditionBlock)
            entryBlock.seal()
            val condition = conditionBlock.addStatement(new IRMethodInvocation(cfg, "hasNext", Some(tempIte), Seq(), Set(node))).target
            val thenBlock = cfg.createStatements()
            val next = thenBlock.addStatement(new IRMethodInvocation(cfg, "next", Some(tempIte), Seq(), Set(node))).target
            cfg.writeVar(v.getVariables.get(0).getName.asString(), thenBlock, next)
            val elseBlock = cfg.createStatements()
            val branch = cfg.createBranch(condition, thenBlock, elseBlock)
            branch.seal()
            conditionBlock.setNext(branch)
            // TODO next
            val newContext = visitStatement(cfg)(cfg.createContext(thenBlock, Some(elseBlock), Some(entryBlock)), b)
            newContext.block.seal()
            newContext.block.setNext(conditionBlock)
            conditionBlock.seal()
            cfg.createContext(elseBlock)
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
        case LabeledStmt(_, s) =>
            // TODO: label
            visitStatement(cfg)(context, s)
        case LocalClassDeclarationStmt(_) =>
            // TODO: Local class decl
            context
        case ReturnStmt(expression) =>
            val expr = expression.map(node => visitExpression(cfg)(context.block, node))
            context.block.addStatement(new IRReturn(expr, Set(node)))
            context
        case SwitchStmt(s, es) =>
            val selector = visitExpression(cfg)(context.block, s)
            context.block.seal()
            val switch = cfg.createSwitch(selector)
            context.block.setNext(switch)
            val exitBlock = cfg.createStatements()
            // TODO: fall through
            es.foreach {
                case SwitchEntryStmt(Some(l), s) =>
                    val label = visitExpression(cfg)(context.block, l)
                    val statements = cfg.createStatements()
                    switch(ExpressionLabel(label)) = statements
                    val newContext = (cfg.createContext(statements, Some(exitBlock), None) /: s)((c, st) => visitStatement(cfg)(c, st))
                    newContext.block.seal()
                    newContext.block.setNext(exitBlock)
                case SwitchEntryStmt(None, s) =>
                    val statements = cfg.createStatements()
                    switch(DefaultLabel) = statements
                    val newContext = (cfg.createContext(statements, Some(exitBlock), None) /: s)((c, st) => visitStatement(cfg)(c, st))
                    newContext.block.seal()
                    newContext.block.setNext(exitBlock)
            }
            context.copy(block = exitBlock)
        case SynchronizedStmt(_, b) =>
            visitStatement(cfg)(context, b)
        case ThrowStmt(e) =>
            val exception = visitExpression(cfg)(context.block, e)
            context.block.addStatement(new IRThrow(exception, Set(node)))
            context
        case TryStmt(r, t, _, f) =>
            // TODO
            r.foreach(visitExpression(cfg)(context.block, _))
            var newContext = visitStatement(cfg)(context, t)
            //            newContext = (newContext /: cs) ((c, n) => visitCatchClause(cfg)(c, n))
            newContext = (newContext /: f) ((c, n) => visitStatement(cfg)(c, n))
            newContext
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
    }

    def visitExpression(cfg: CFG)(block: CFGStatements, node: Expression[_]): IRExpression = node match {
        case ArrayAccessExpr(n, i) =>
            val name = visitExpression(cfg)(block, n)
            val index = visitExpression(cfg)(block, i)
            block.addStatement(new IRArrayAccess(cfg, name, index, Set(node))).target
        case ArrayCreationExpr(ty, lvls, init) =>
            val levels = lvls.flatMap(e => e.dimension.map(d => visitExpression(cfg)(block, d)))
            val initializers = init.toList.flatMap(i => i.values.map(e => visitExpression(cfg)(block, e)))
            block.addStatement(new IRMethodInvocation(cfg, "<init>[]", Some(IRTypeObject(ty)), levels ++ initializers, Set(node))).target
        case ArrayInitializerExpr(vs) =>
            val values = vs.map(visitExpression(cfg)(block, _))
            IRArray(values)
        case AssignExpr(NameExpr(name), ope, s) =>
            val source = visitExpression(cfg)(block, s)
            ope match {
                case AssignExpr.Operator.Assign =>
                    cfg.writeVar(name, block, source)
                    source
                case _ =>
                    val value = cfg.readVar(name, block)
                    val target = block.addStatement(new IRBinaryOperation(cfg, BinaryOperator.fromAssignExprOperator(ope), value, source, Set(node))).target
                    cfg.writeVar(name, block, target)
                    target
            }
        case AssignExpr(name, ope, s) =>
            // TODO: left hand side
            visitExpression(cfg)(block, name)
        case BinaryExpr(l, ope, r) =>
            val lhs = visitExpression(cfg)(block, l)
            val rhs = visitExpression(cfg)(block, r)
            block.addStatement(new IRBinaryOperation(cfg, BinaryOperator.fromBinaryExprOperator(ope), lhs, rhs, Set(node))).target
        case BooleanLiteralExpr(true) => IRExpression.True
        case BooleanLiteralExpr(false) => IRExpression.False
        case CastExpr(_, e) =>
            visitExpression(cfg)(block, e)
        case ConditionalExpr(c, t, e) =>
            val condition = visitExpression(cfg)(block, c)
            val thenExpr = visitExpression(cfg)(block, t)
            val elseExpr = visitExpression(cfg)(block, e)
            block.addStatement(new IRConditionalExpr(cfg, condition, thenExpr, elseExpr, Set(node))).target
        case CharLiteralExpr(n) => IRChar(n)
        case ClassExpr(t) =>
            IRTypeObject(t)
        case DoubleLiteralExpr(n) => IRDouble(n)
        case EnclosedExpr(e) =>
            visitExpression(cfg)(block, e)
        case FieldAccessExpr(scope, name, _) =>
            val receiver = visitExpression(cfg)(block, scope)
            block.addStatement(new IRFieldAccess(cfg, receiver, name, Set(node))).target
        case InstanceOfExpr(e, t) =>
            val expression = visitExpression(cfg)(block, e)
            block.addStatement(new IRInstanceOf(cfg, expression, t, Set(node))).target
        case IntegerLiteralExpr(n) => IRInteger(n, Set(node))
        case LambdaExpr(_, _, _) => IRLambda // TODO: lambda expr
        case LongLiteralExpr(n) => IRLong(n)
        case n @ MethodCallExpr(_, scope, _, arguments) =>
            val receiver = scope.map(node => visitExpression(cfg)(block, node))
            val args = arguments.map(node => visitExpression(cfg)(block, node))
            block.addStatement(new IRMethodInvocation(cfg, n.delegate.getName.getIdentifier, receiver, args, Set(node))).target
        case MethodReferenceExpr(_, _, _) =>
            IRMethodReference
        case NameExpr(name) =>
            cfg.readVar(name, block) match {
                case IRUndef => IRExtern(name)
                case e => e
            }
        case NullLiteralExpr() => IRNull
        case ObjectCreationExpr(_, ty, _, arguments, _) =>
            val args = arguments.map(node => visitExpression(cfg)(block, node))
            block.addStatement(new IRMethodInvocation(cfg, s"$ty::<init>", Some(IRTypeObject(ty)), args, Set(node))).target
        case StringLiteralExpr(n) => IRString(n)
        case SuperExpr(_) =>
            // TODO: more specific
            IRSuper
        case ThisExpr(_) =>
            // TODO: more specific
            IRThis
        case UnaryExpr(JPUnaryExpr.Operator.PREFIX_INCREMENT, NameExpr(name)) =>
            val source = cfg.readVar(name, block)
            val target = block.addStatement(new IRBinaryOperation(cfg, BinaryOperator.Plus, source, IRInteger(1, Set(node)), Set(node))).target
            cfg.writeVar(name, block, target)
            target
        case UnaryExpr(JPUnaryExpr.Operator.POSTFIX_INCREMENT, NameExpr(name)) =>
            val source = cfg.readVar(name, block)
            val target = block.addStatement(new IRBinaryOperation(cfg, BinaryOperator.Plus, source, IRInteger(1, Set(node)), Set(node))).target
            cfg.writeVar(name, block, target)
            source
        case UnaryExpr(ope, e) =>
            val source = visitExpression(cfg)(block, e)
            block.addStatement(new IRUnaryOperation(cfg, UnaryOperator.fromAssignExprOperator(ope), source, Set(node))).target
        case VariableDeclarationExpr(declarators) =>
            declarators.map(visitVariableDeclarator(cfg)(block, _))
            null // TODO
        case TypeExpr(ty) =>
            IRTypeObject(ty)
    }

}
