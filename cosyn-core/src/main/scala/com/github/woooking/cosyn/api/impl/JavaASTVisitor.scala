package com.github.woooking.cosyn.api.impl

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.{UnaryExpr => JPUnaryExpr}
import com.github.javaparser.ast.stmt.CatchClause
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGSwitch.{DefaultLabel, ExpressionLabel}
import com.github.woooking.cosyn.dfgprocessor.cfg.{CFGImpl, CFGStatements}
import com.github.woooking.cosyn.dfgprocessor.ir._
import com.github.woooking.cosyn.dfgprocessor.ir.statements._
import com.github.woooking.cosyn.javaparser.CompilationUnit
import com.github.woooking.cosyn.javaparser.body._
import com.github.woooking.cosyn.javaparser.expr._
import com.github.woooking.cosyn.javaparser.stmt._

import scala.annotation.tailrec

class JavaASTVisitor {
    def resolveParameterType(p: Parameter): String = p.getType.asString()

    def resolveType(ty: Type): String = ty.asString()

    def resolveMethodCallExpr(methodCallExpr: MethodCallExpr): String = methodCallExpr.name

    def resolveObjectCreationExpr(objectCreationExpr: ObjectCreationExpr): String = s"${objectCreationExpr.ty}::init"

    def generateCFGs(cu: CompilationUnit): Seq[CFGImpl] = cu match {
        case CompilationUnit(None, _, _, tys) =>
            generateCFGs(cu.file, "", tys, Seq.empty)
        case CompilationUnit(Some(p), _, _, tys) =>
            generateCFGs(cu.file, s"${p.delegate.getName.asString()}.", tys, Seq.empty)
    }

    @tailrec
    private def generateCFGs(file: String, qualifier: String, bodyDecls: List[BodyDeclaration[_]], methods: Seq[CFGImpl]): Seq[CFGImpl] = bodyDecls match {
        case Nil => methods
        case body :: rest => generateCFGs(file, qualifier, rest, methods ++ generateCFGs(file, qualifier, body))
    }

    private def generateCFGs(file: String, qualifier: String, bodyDecl: BodyDeclaration[_]): Seq[CFGImpl] = bodyDecl match {
        case AnnotationDeclaration(_, _) => Seq.empty
        case ClassOrInterfaceDeclaration(name, _, _, _, typeDecls) => generateCFGs(file, s"$qualifier$name.", typeDecls, Seq.empty)
        case FieldDeclaration(_) => Seq.empty
        case InitializerDeclaration(_, _) => Seq.empty
        case d@MethodDeclaration(_, _, _, _, _, _, Some(_)) =>
            val cfg = generateCFG(file, qualifier, d)
            Seq(cfg)
        case MethodDeclaration(_, _, _, _, _, _, None) =>
            Seq.empty
        case d@ConstructorDeclaration(_, _, _, _, _, _) =>
            val cfg = generateCFG(file, qualifier, d)
            Seq(cfg)
        case EnumDeclaration(_, _, _, _) =>
            // TODO: enum
            Seq.empty
    }

    def generateCFG(file: String, qualifier: String, decl: ConstructorDeclaration): CFGImpl = {
//        val cfg = new CFGImpl(file, s"$qualifier${decl.signature}", decl)
//        decl.params.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, resolveParameterType(p))))
//        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl.body)
//        pair.block.seal()
//        pair.block.setNext(cfg.exit)
//        cfg
        ???
    }

    def generateCFG(file: String, qualifier: String, decl: MethodDeclaration): CFGImpl = {
//        val cfg = new CFGImpl(file, s"$qualifier${decl.signature}", decl)
//        decl.params.foreach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, resolveParameterType(p))))
//        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl.body.get)
//        pair.block.seal()
//        pair.block.setNext(cfg.exit)
//        cfg
        ???
    }

    def generateCFG(decl: BlockStmt): CFGImpl = {
//        val cfg = new CFGImpl("", "", decl)
//        val pair = visitStatement(cfg)(cfg.createContext(cfg.entry), decl)
//        pair.block.seal()
//        pair.block.setNext(cfg.exit)
//        cfg
        ???
    }

    def visitVariableDeclarator(cfg: CFGImpl)(block: CFGStatements, node: VariableDeclarator): IRExpression = node match {
        case VariableDeclarator(name, _, initializer) =>
            val initValue = initializer.map(node => visitExpression(cfg)(block, node)).getOrElse(IRNull(node))
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

    def visitCatchClause(cfg: CFGImpl)(block: cfg.Context, node: CatchClause): cfg.Context = ???

    def visitStatement(cfg: CFGImpl)(context: cfg.Context, node: Statement): cfg.Context = node match {
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
            val tempIte = entryBlock.addStatement(IRMethodInvocation(cfg, "iterator", Some(iteExpr), Seq(), Set(node))).target
            val conditionBlock = cfg.createStatements()
            entryBlock.setNext(conditionBlock)
            entryBlock.seal()
            val condition = conditionBlock.addStatement(IRMethodInvocation(cfg, "hasNext", Some(tempIte), Seq(), Set(node))).target
            val thenBlock = cfg.createStatements()
            val next = thenBlock.addStatement(IRMethodInvocation(cfg, "next", Some(tempIte), Seq(), Set(node))).target
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
                    val newContext = (cfg.createContext(statements, Some(exitBlock), None) /: s) ((c, st) => visitStatement(cfg)(c, st))
                    newContext.block.seal()
                    newContext.block.setNext(exitBlock)
                case SwitchEntryStmt(None, s) =>
                    val statements = cfg.createStatements()
                    switch(DefaultLabel) = statements
                    val newContext = (cfg.createContext(statements, Some(exitBlock), None) /: s) ((c, st) => visitStatement(cfg)(c, st))
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

    def visitFieldAccess(cfg: CFGImpl)(block: CFGStatements, node: FieldAccessExpr): IRExpression = {
        val FieldAccessExpr(scope, name, _) = node
        val receiver = visitExpression(cfg)(block, scope)
        block.addStatement(new IRFieldAccess(cfg, receiver, name, Set(node))).target
    }

    def visitNameExpr(cfg: CFGImpl)(block: CFGStatements, node: NameExpr): IRExpression = {
        val name = node.name
        cfg.readVar(name, block) match {
            case IRUndef => IRExtern(name)
            case e => e
        }
    }

    def visitExpression(cfg: CFGImpl)(block: CFGStatements, node: Expression[_ <: Node]): IRExpression = node match {
        case ArrayAccessExpr(n, i) =>
            val name = visitExpression(cfg)(block, n)
            val index = visitExpression(cfg)(block, i)
            block.addStatement(new IRArrayAccess(cfg, name, index, Set(node))).target
        case ArrayCreationExpr(ty, lvls, init) =>
            val levels = lvls.flatMap(e => e.dimension.map(d => visitExpression(cfg)(block, d)))
            val initializers = init.toList.flatMap(i => i.values.map(e => visitExpression(cfg)(block, e)))
            block.addStatement(IRMethodInvocation(cfg, "<init>[]", Some(IRTypeObject(ty.asString(), node)), levels ++ initializers, Set(node))).target
        case ArrayInitializerExpr(vs) =>
            val values = vs.map(visitExpression(cfg)(block, _))
            IRArray(values, node)
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
        case BooleanLiteralExpr(true) => IRBoolean(true, node)
        case BooleanLiteralExpr(false) => IRBoolean(false, node)
        case CastExpr(_, e) =>
            visitExpression(cfg)(block, e)
        case ConditionalExpr(c, t, e) =>
            val condition = visitExpression(cfg)(block, c)
            val thenExpr = visitExpression(cfg)(block, t)
            val elseExpr = visitExpression(cfg)(block, e)
            block.addStatement(new IRConditionalExpr(cfg, condition, thenExpr, elseExpr, Set(node))).target
        case CharLiteralExpr(n) => IRChar(n, node)
        case ClassExpr(t) =>
            IRTypeObject(t.asString(), node)
        case DoubleLiteralExpr(n) => IRDouble(n, node)
        case EnclosedExpr(e) =>
            visitExpression(cfg)(block, e)
        case n: FieldAccessExpr => visitFieldAccess(cfg)(block, n)
        case InstanceOfExpr(e, t) =>
            val expression = visitExpression(cfg)(block, e)
            block.addStatement(new IRInstanceOf(cfg, expression, t, Set(node))).target
        case IntegerLiteralExpr(n) => IRInteger(n, node)
        case LambdaExpr(_, _, _) => IRLambda // TODO: lambda expr
        case LongLiteralExpr(n) => IRLong(n, node)
        case n@MethodCallExpr(_, scope, _, arguments) =>
            val receiver = scope.map(node => visitExpression(cfg)(block, node))
            val args = arguments.map(node => visitExpression(cfg)(block, node))
            block.addStatement(IRMethodInvocation(cfg, resolveMethodCallExpr(n), receiver, args, Set(node))).target
        case MethodReferenceExpr(_, _, _) =>
            IRMethodReference
        case n: NameExpr => visitNameExpr(cfg)(block, n)
        case NullLiteralExpr() => IRNull(node)
        case n @ ObjectCreationExpr(_, ty, _, arguments, _) =>
            val args = arguments.map(node => visitExpression(cfg)(block, node))
            block.addStatement(IRMethodInvocation(cfg, resolveObjectCreationExpr(n), None, args, Set(node))).target
        case StringLiteralExpr(n) => IRString(n, node)
        case SuperExpr(_) =>
            // TODO: more specific
            IRSuper(node)
        case ThisExpr(_) =>
            // TODO: more specific
            IRThis(node)
        case UnaryExpr(JPUnaryExpr.Operator.PREFIX_INCREMENT, NameExpr(name)) =>
            val source = cfg.readVar(name, block)
            val target = block.addStatement(new IRBinaryOperation(cfg, BinaryOperator.Plus, source, IRInteger(1, node), Set(node))).target
            cfg.writeVar(name, block, target)
            target
        case UnaryExpr(JPUnaryExpr.Operator.POSTFIX_INCREMENT, NameExpr(name)) =>
            val source = cfg.readVar(name, block)
            val target = block.addStatement(new IRBinaryOperation(cfg, BinaryOperator.Plus, source, IRInteger(1, node), Set(node))).target
            cfg.writeVar(name, block, target)
            source
        case UnaryExpr(ope, e) =>
            val source = visitExpression(cfg)(block, e)
            block.addStatement(new IRUnaryOperation(cfg, UnaryOperator.fromAssignExprOperator(ope), source, Set(node))).target
        case VariableDeclarationExpr(declarators) =>
            declarators.map(visitVariableDeclarator(cfg)(block, _))
            null // TODO
        case TypeExpr(ty) =>
            IRTypeObject(ty.asString(), node)
    }

}
