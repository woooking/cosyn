package com.github.woooking.cosyn.pattern.java

import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults
import com.github.javaparser.ast.{Node, NodeList}
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.{JavaParserConstructorDeclaration, JavaParserEnumConstantDeclaration, JavaParserFieldDeclaration}
import com.github.javaparser.symbolsolver.reflectionmodel.{ReflectionConstructorDeclaration, ReflectionFieldDeclaration}
import com.github.woooking.cosyn.pattern.KnowledgeGraph
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.{CFGImpl, CFGStatements}
import com.github.woooking.cosyn.pattern.dfgprocessor.ir
import com.github.woooking.cosyn.pattern.dfgprocessor.ir._
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements._
import com.github.woooking.cosyn.pattern.util.OptionConverters._
import org.slf4s.Logging

import scala.collection.JavaConverters._

class JavaExpressionVisitor(val cfg: CFGImpl) extends GenericVisitorWithDefaults[IRExpression, CFGStatements] with Logging {
    private def assignOpe2BinaryOpe(ope: AssignExpr.Operator): BinaryExpr.Operator = ope match {
        case AssignExpr.Operator.PLUS => BinaryExpr.Operator.PLUS
        case AssignExpr.Operator.MINUS => BinaryExpr.Operator.MINUS
        case AssignExpr.Operator.MULTIPLY => BinaryExpr.Operator.MULTIPLY
        case AssignExpr.Operator.DIVIDE => BinaryExpr.Operator.DIVIDE
        case AssignExpr.Operator.BINARY_AND => BinaryExpr.Operator.BINARY_AND
        case AssignExpr.Operator.BINARY_OR => BinaryExpr.Operator.BINARY_OR
        case AssignExpr.Operator.XOR => BinaryExpr.Operator.XOR
        case AssignExpr.Operator.REMAINDER => BinaryExpr.Operator.REMAINDER
        case AssignExpr.Operator.LEFT_SHIFT => BinaryExpr.Operator.LEFT_SHIFT
        case AssignExpr.Operator.SIGNED_RIGHT_SHIFT => BinaryExpr.Operator.SIGNED_RIGHT_SHIFT
        case AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT => BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT
        case AssignExpr.Operator.ASSIGN => throw new Exception("Cannot convert assign operator to binary operator!")
    }

    private def resolveMethodCallExpr(methodCallExpr: MethodCallExpr): String = {
        try {
            KnowledgeGraph.getMethodProto(methodCallExpr.resolve().getQualifiedSignature)
        } catch {
            case _: UnsolvedSymbolException =>
                methodCallExpr.getName.asString()
            case e: Throwable =>
                log.warn("Unexpected Exception!", e)
                methodCallExpr.getName.asString()
        }
    }

    private def resolveObjectCreationExpr(objectCreationExpr: ObjectCreationExpr): String = {
        try {
            objectCreationExpr.resolve() match {
                case d: JavaParserConstructorDeclaration[_] =>
                    d.getQualifiedSignature
            }
        } catch {
            case e: UnsolvedSymbolException =>
                //                println("-----")
                //                println(methodCallExpr.getName.asString())
                //                println(cfg.decl)
                //                e.printStackTrace()
                objectCreationExpr.getType.asString()
            case e: Throwable =>
                //                println("-----")
                //                println(methodCallExpr.getName.asString())
                //                println(cfg.decl)
                //                e.printStackTrace()
                objectCreationExpr.getType.asString()
        }
    }

    private def posInfo(n: Node) = {
        n.getBegin.asScala match {
            case Some(pos) => s"line: ${pos.line}, col: ${pos.column}"
            case None => ""
        }
    }

    private def unimplemented(msg: String, n: Node): Unit = {
        log.warn(s"$msg not supported ${posInfo(n)}")
    }

    override def defaultAction(n: Node, arg: CFGStatements): IRExpression = ???

    override def defaultAction(n: NodeList[_ <: Node], arg: CFGStatements): IRExpression = ???

    override def visit(n: ArrayAccessExpr, block: CFGStatements): IRExpression = {
        val name = n.getName.accept(this, block)
        val index = n.getIndex.accept(this, block)
        block.addStatement(new IRArrayAccess(cfg, name, index, Set(n))).target
    }

    override def visit(n: ArrayCreationExpr, block: CFGStatements): IRExpression = {
        val levels = n.getLevels.asScala.flatMap(_.getDimension.asScala.map(_.accept(this, block)))
        val initializers = n.getInitializer.asScala.toList.flatMap(_.getValues.asScala.map(_.accept(this, block)))
        block.addStatement(IRMethodInvocation(cfg, "<init>[]", Some(IRTypeObject(n.getElementType.asString(), n)), levels ++ initializers, Set(n))).target
    }

    override def visit(n: ArrayInitializerExpr, block: CFGStatements): IRExpression = {
        val values = n.getValues.asScala.map(v => v.accept(this, block))
        IRArray(values.toList, n)
    }

    override def visit(n: AssignExpr, block: CFGStatements): IRExpression = {
        n.getTarget match {
            case nameExpr: NameExpr =>
                val source = n.getValue.accept(this, block)
                n.getOperator match {
                    case AssignExpr.Operator.ASSIGN =>
                        cfg.writeVar(nameExpr.getName.asString(), block, source)
                        source
                    case ope =>
                        val value = cfg.readVar(nameExpr.getName.asString(), block)
                        val target = block.addStatement(new IRBinaryOperation(cfg, assignOpe2BinaryOpe(ope), value, source, Set(n))).target
                        cfg.writeVar(nameExpr.getName.asString(), block, target)
                        target
                }
            case fieldAccessExpr: FieldAccessExpr if fieldAccessExpr.getScope.isThisExpr =>
                val source = n.getValue.accept(this, block)
                n.getOperator match {
                    case AssignExpr.Operator.ASSIGN =>
                        cfg.writeVar(fieldAccessExpr.getName.asString(), block, source)
                        source
                    case ope =>
                        val value = cfg.readVar(fieldAccessExpr.getName.asString(), block)
                        val target = block.addStatement(new IRBinaryOperation(cfg, assignOpe2BinaryOpe(ope), value, source, Set(n))).target
                        cfg.writeVar(fieldAccessExpr.getName.asString(), block, target)
                        target
                }
            case _ =>
                // TODO: left hand side
                unimplemented("AssignExpr with non NameExpr left hand", n)
                n.getValue.accept(this, block)
        }
    }

    override def visit(n: BinaryExpr, block: CFGStatements): IRExpression = {
        val lhs = n.getLeft.accept(this, block)
        val rhs = n.getRight.accept(this, block)
        block.addStatement(new IRBinaryOperation(cfg, n.getOperator, lhs, rhs, Set(n))).target
    }

    override def visit(n: BooleanLiteralExpr, block: CFGStatements): IRExpression = {
        IRBoolean(n.getValue, n)
    }

    override def visit(n: CastExpr, block: CFGStatements): IRExpression = {
        n.getExpression.accept(this, block)
    }

    override def visit(n: CharLiteralExpr, block: CFGStatements): IRExpression = {
        IRChar(n.asChar(), n)
    }

    override def visit(n: ClassExpr, block: CFGStatements): IRExpression = {
        IRTypeObject(n.getType.asString(), n)
    }

    override def visit(n: ConditionalExpr, block: CFGStatements): IRExpression = {
        val condition = n.getCondition.accept(this, block)
        val thenExpr = n.getThenExpr.accept(this, block)
        val elseExpr = n.getElseExpr.accept(this, block)
        block.addStatement(new IRConditionalExpr(cfg, condition, thenExpr, elseExpr, Set(n))).target
    }

    override def visit(n: DoubleLiteralExpr, block: CFGStatements): IRExpression = {
        IRDouble(n.asDouble(), n)
    }

    override def visit(n: EnclosedExpr, block: CFGStatements): IRExpression = {
        n.getInner.accept(this, block)
    }

    override def visit(n: FieldAccessExpr, block: CFGStatements): IRExpression = {
        try {
            n.resolve() match {
                case d: ReflectionFieldDeclaration if d.isStatic =>
                    val receiver = IRTypeObject(n.getScope.asNameExpr().resolve().asType().getQualifiedName, n.getScope)
                    val name = n.getName.asString()
                    block.addStatement(new IRStaticFieldAccess(cfg, receiver, name, Set(n))).target
                case d: JavaParserFieldDeclaration if d.isStatic =>
                    val receiver = IRTypeObject(n.getScope.asNameExpr().resolve().asType().getQualifiedName, n.getScope)
                    val name = n.getName.asString()
                    block.addStatement(new IRStaticFieldAccess(cfg, receiver, name, Set(n))).target
                case d: JavaParserEnumConstantDeclaration =>
                    val value = IREnum(d.getType.asReferenceType().getQualifiedName, d.getName)
                    block.addStatement(new IRAssignment(cfg, value, Set(n))).target
                case _ =>
                    val receiver = n.getScope.accept(this, block)
                    val name = n.getName.asString()
                    block.addStatement(new IRFieldAccess(cfg, receiver, name, Set(n))).target
            }
        } catch {
            case e: Throwable =>
//                e.printStackTrace()
                val receiver = n.getScope.accept(this, block)
                val name = n.getName.asString()
                block.addStatement(new IRFieldAccess(cfg, receiver, name, Set(n))).target
        }

    }

    override def visit(n: InstanceOfExpr, block: CFGStatements): IRExpression = {
        val expr = n.getExpression.accept(this, block)
        block.addStatement(new IRInstanceOf(cfg, expr, n.getType, Set(n))).target
    }

    override def visit(n: IntegerLiteralExpr, block: CFGStatements): IRExpression = {
        IRInteger(n.asInt(), n)
    }

    override def visit(n: LambdaExpr, block: CFGStatements): IRExpression = {
        log.warn("LambdaExpr not supported")
        IRLambda // TODO: lambda expr
    }

    override def visit(n: LongLiteralExpr, block: CFGStatements): IRExpression = {
        IRLong(n.asLong(), n)
    }

    override def visit(n: MethodCallExpr, block: CFGStatements): IRExpression = {
        val receiver = n.getScope.asScala.map(node => node.accept(this, block))
        val args = n.getArguments.asScala.map(node => node.accept(this, block))
        block.addStatement(IRMethodInvocation(cfg, resolveMethodCallExpr(n), receiver, args, Set(n))).target
    }

    override def visit(n: MethodReferenceExpr, block: CFGStatements): IRExpression = {
        log.warn("MethodReferenceExpr not supported")
        IRMethodReference // TODO: MethodReferenceExpr
    }

    override def visit(n: NameExpr, block: CFGStatements): IRExpression = {
        val name = n.getName.asString()
        cfg.readVar(name, block) match {
            case IRUndef => IRExtern(name)
            case e => e
        }
    }

    override def visit(n: NullLiteralExpr, block: CFGStatements): IRExpression = {
        IRNull(n)
    }

    override def visit(n: ObjectCreationExpr, block: CFGStatements): IRExpression = {
        val args = n.getArguments.asScala.map(node => node.accept(this, block))
        block.addStatement(IRMethodInvocation(cfg, resolveObjectCreationExpr(n), None, args, Set(n))).target
    }

    override def visit(n: StringLiteralExpr, block: CFGStatements): IRExpression = {
        IRString(n.asString(), n)
    }

    override def visit(n: SuperExpr, block: CFGStatements): IRExpression = {
        // TODO: more specific
        IRSuper(n)
    }

    override def visit(n: ThisExpr, block: CFGStatements): IRExpression = {
        // TODO: more specific
        IRThis(n)
    }

    override def visit(n: UnaryExpr, block: CFGStatements): IRExpression = {
        n.getOperator match {
            case UnaryExpr.Operator.PREFIX_INCREMENT |
                 UnaryExpr.Operator.POSTFIX_INCREMENT |
                 UnaryExpr.Operator.PREFIX_DECREMENT |
                 UnaryExpr.Operator.POSTFIX_DECREMENT =>
                val name = n.getExpression match {
                    case nameExpr: NameExpr => nameExpr.getName.asString()
                    case fieldAccessExpr: FieldAccessExpr if fieldAccessExpr.getScope.isThisExpr => fieldAccessExpr.getName.asString()
                    case x =>
                        log.warn(s"Assign Expr of $x not implemented.")
                        ""
                }
                //                val name = n.getExpression.asNameExpr().getName.asString()
                val source = cfg.readVar(name, block)
                val ope = if (n.getOperator == UnaryExpr.Operator.PREFIX_INCREMENT || n.getOperator == UnaryExpr.Operator.POSTFIX_INCREMENT)
                    BinaryExpr.Operator.PLUS
                else
                    BinaryExpr.Operator.MINUS

                val target = block.addStatement(new IRBinaryOperation(cfg, ope, source, IRInteger(1, n), Set(n))).target
                cfg.writeVar(name, block, target)
                if (n.getOperator == UnaryExpr.Operator.PREFIX_INCREMENT || n.getOperator == UnaryExpr.Operator.PREFIX_DECREMENT)
                    target
                else
                    source
            case ope =>
                val source = n.getExpression.accept(this, block)
                block.addStatement(new IRUnaryOperation(cfg, ope, source, Set(n))).target
        }
    }

    override def visit(n: VariableDeclarationExpr, block: CFGStatements): IRExpression = {
        n.getVariables.asScala.foreach(_.accept(this, block))
        null // TODO
    }

    override def visit(n: TypeExpr, block: CFGStatements): IRExpression = {
        IRTypeObject(n.getType.asString(), n)
    }

    override def visit(n: VariableDeclarator, block: CFGStatements): IRExpression = {
        val initValue = n.getInitializer.asScala.map(_.accept(this, block)).getOrElse(IRNull(n))
        val name = n.getName.asString()
        initValue match {
            case t: IRTemp =>
                cfg.writeVar(name, block, t)
                t
            case _ =>
                val target = block.addStatement(new IRAssignment(cfg, initValue, Set(n))).target
                cfg.writeVar(name, block, target)
                target
        }
    }

}
