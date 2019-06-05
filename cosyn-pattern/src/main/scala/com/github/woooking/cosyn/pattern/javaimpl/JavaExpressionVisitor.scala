package com.github.woooking.cosyn.pattern.javaimpl

import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults
import com.github.javaparser.ast.{Node, NodeList}
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.{JavaParserConstructorDeclaration, JavaParserEnumConstantDeclaration, JavaParserFieldDeclaration}
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionFieldDeclaration
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.pattern.{Components, CosynConfig}
import com.github.woooking.cosyn.pattern.javaimpl.cfg.{CFG, CFGStatements}
import com.github.woooking.cosyn.pattern.javaimpl.ir._
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements._
import CodeUtil.typeOf
import org.slf4s.Logging

import scala.compat.java8.OptionConverters._
import scala.collection.JavaConverters._
import scala.util.Try

class JavaExpressionVisitor(val cfg: CFG) extends GenericVisitorWithDefaults[Option[IRExpression], CFGStatements] with Logging {
    private val methodEntityRepository = Components.methodEntityRepository

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
            methodEntityRepository.getMethodProto(methodCallExpr.resolve().getQualifiedSignature)
        } catch {
            case _: UnsolvedSymbolException =>
                methodCallExpr.getName.asString()
            case e: Throwable =>
                if (CosynConfig.global.printParseErrors) log.warn("Unexpected Exception!", e)
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
            case _: UnsolvedSymbolException =>
                //                println("-----")
                //                println(methodCallExpr.getName.asString())
                //                println(cfg.decl)
                //                e.printStackTrace()
                objectCreationExpr.getType.asString()
            case _: Throwable =>
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

    override def defaultAction(n: Node, arg: CFGStatements): Option[IRExpression] = ???

    override def defaultAction(n: NodeList[_ <: Node], arg: CFGStatements): Option[IRExpression] = ???

    override def visit(n: ArrayAccessExpr, block: CFGStatements): Option[IRExpression] = {
        for (
            name <- n.getName.accept(this, block);
            index <- n.getIndex.accept(this, block);
            ty <- typeOf(n)
        ) yield block.addStatement(new IRArrayAccess(cfg, ty, name, index, Set(n))).target
    }

    override def visit(n: ArrayCreationExpr, block: CFGStatements): Option[IRExpression] = {
        val levels = n.getLevels.asScala.flatMap(_.getDimension.asScala.flatMap(_.accept(this, block)).toList)
        val initializers = n.getInitializer.asScala.toList.flatMap(_.getValues.asScala.flatMap(_.accept(this, block).toList))
        for (
            ty <- typeOf(n)
        ) yield block.addStatement(IRMethodInvocation(cfg, ty, "<init>[]", Some(IRTypeObject(n.getElementType.asString(), n)), levels ++ initializers, Set(n))).target
    }

    override def visit(n: ArrayInitializerExpr, block: CFGStatements): Option[IRExpression] = {
        val values = n.getValues.asScala.flatMap(_.accept(this, block).toList)
        IRArray(values.toList, n)
    }

    override def visit(n: AssignExpr, block: CFGStatements): Option[IRExpression] = {
        n.getTarget match {
            case nameExpr: NameExpr =>
                for (
                    source <- n.getValue.accept(this, block);
                    ty <- typeOf(nameExpr)
                ) yield n.getOperator match {
                    case AssignExpr.Operator.ASSIGN =>
                        cfg.writeVar(nameExpr.getName.asString(), block, source)
                        source
                    case ope =>
                        val value = cfg.readVar(ty, nameExpr.getName.asString(), block)
                        val target = block.addStatement(new IRBinaryOperation(cfg, ty, assignOpe2BinaryOpe(ope), value, source, Set(n))).target
                        cfg.writeVar(nameExpr.getName.asString(), block, target)
                        target
                }
            case fieldAccessExpr: FieldAccessExpr if fieldAccessExpr.getScope.isThisExpr =>
                for (
                    source <- n.getValue.accept(this, block);
                    ty <- typeOf(fieldAccessExpr)
                ) yield n.getOperator match {
                    case AssignExpr.Operator.ASSIGN =>
                        cfg.writeVar(fieldAccessExpr.getName.asString(), block, source)
                        source
                    case ope =>
                        val value = cfg.readVar(ty, fieldAccessExpr.getName.asString(), block)
                        val target = block.addStatement(new IRBinaryOperation(cfg, ty, assignOpe2BinaryOpe(ope), value, source, Set(n))).target
                        cfg.writeVar(fieldAccessExpr.getName.asString(), block, target)
                        target
                }
            case _ =>
                // TODO: left hand side
                unimplemented("AssignExpr with non NameExpr left hand", n)
                n.getValue.accept(this, block)
        }
    }

    override def visit(n: BinaryExpr, block: CFGStatements): Option[IRExpression] = {
        for (
            lhs <- n.getLeft.accept(this, block);
            rhs <- n.getRight.accept(this, block);
            ty <- typeOf(n)
        ) yield {
            block.addStatement(new IRBinaryOperation(cfg, ty, n.getOperator, lhs, rhs, Set(n))).target
        }
    }

    override def visit(n: BooleanLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        IRBoolean(n.getValue, n)
    }

    override def visit(n: CastExpr, block: CFGStatements): Option[IRExpression] = {
        n.getExpression.accept(this, block)
    }

    override def visit(n: CharLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        IRChar(n.asChar(), n)
    }

    override def visit(n: ClassExpr, block: CFGStatements): Option[IRExpression] = {
        IRTypeObject(n.getType.asString(), n)
    }

    override def visit(n: ConditionalExpr, block: CFGStatements): Option[IRExpression] = {
        for (
            condition <- n.getCondition.accept(this, block);
            thenExpr <- n.getThenExpr.accept(this, block);
            elseExpr <- n.getElseExpr.accept(this, block);
            ty <- typeOf(n)
        ) yield {
            block.addStatement(new IRConditionalExpr(cfg, ty, condition, thenExpr, elseExpr, Set(n))).target
        }
    }

    override def visit(n: DoubleLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        IRDouble(n.asDouble(), n)
    }

    override def visit(n: EnclosedExpr, block: CFGStatements): Option[IRExpression] = {
        n.getInner.accept(this, block)
    }

    override def visit(n: FieldAccessExpr, block: CFGStatements): Option[IRExpression] = {
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
                    val value = IREnum(d.getName, d.getType.asReferenceType().getQualifiedName, n.getName)
                    block.addStatement(new IREnumAccess(cfg, d.getType.asReferenceType().getQualifiedName, value, Set(n))).target
                case _ =>
                    for (receiver <- n.getScope.accept(this, block)) yield {
                        val name = n.getName.asString()
                        val ty = CodeUtil.resolvedTypeToType(n.calculateResolvedType()).toString
                        block.addStatement(new IRFieldAccess(cfg, ty, receiver, name, Set(n))).target
                    }
            }
        } catch {
            case _: Throwable =>
                for (receiver <- n.getScope.accept(this, block)) yield {
                    val name = n.getName.asString()
                    block.addStatement(new IRFieldAccess(cfg, "", receiver, name, Set(n))).target
                }
        }

    }

    override def visit(n: InstanceOfExpr, block: CFGStatements): Option[IRExpression] = {
        for (expr <- n.getExpression.accept(this, block)) yield block.addStatement(new IRInstanceOf(cfg, expr, n.getType, Set(n))).target
    }

    override def visit(n: IntegerLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        try {
            IRInteger(n.asInt(), n)
        } catch {
            case _: NumberFormatException =>
                IRLong(2147483648L, n)
        }
    }

    override def visit(n: LambdaExpr, block: CFGStatements): Option[IRExpression] = {
        log.warn("LambdaExpr not supported")
        IRLambda // TODO: lambda expr
    }

    override def visit(n: LongLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        IRLong(n.asLong(), n)
    }

    override def visit(n: MethodCallExpr, block: CFGStatements): Option[IRExpression] = {
        if (Try {n.resolve()}.toOption.exists(_.isStatic)) {
            for (
                ty <- typeOf(n)
            ) yield {
                val args = n.getArguments.asScala.flatMap(_.accept(this, block).toList)
                block.addStatement(IRMethodInvocation(cfg, ty, resolveMethodCallExpr(n), None, args, Set(n))).target
            }
        } else for (
            receiver <- n.getScope.asScala.map(_.accept(this, block));
            ty <- typeOf(n)
        ) yield {
            val args = n.getArguments.asScala.flatMap(_.accept(this, block).toList)
            block.addStatement(IRMethodInvocation(cfg, ty, resolveMethodCallExpr(n), receiver, args, Set(n))).target
        }
    }

    override def visit(n: MethodReferenceExpr, block: CFGStatements): Option[IRExpression] = {
        log.warn("MethodReferenceExpr not supported")
        IRMethodReference // TODO: MethodReferenceExpr
    }

    override def visit(n: NameExpr, block: CFGStatements): Option[IRExpression] = {
        val name = n.getName.asString()
        for (ty <- typeOf(n)) yield cfg.readVar(ty, name, block) match {
            case IRUndef => IRExtern(ty, name, None)
            case e => e
        }
    }

    override def visit(n: NullLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        IRNull(n)
    }

    override def visit(n: ObjectCreationExpr, block: CFGStatements): Option[IRExpression] = {
        for (
            ty <- typeOf(n)
        ) yield {
            val args = n.getArguments.asScala.flatMap(_.accept(this, block).toList)
            block.addStatement(IRMethodInvocation(cfg, ty, resolveObjectCreationExpr(n), None, args, Set(n))).target
        }
    }

    override def visit(n: StringLiteralExpr, block: CFGStatements): Option[IRExpression] = {
        IRString(n.asString(), n)
    }

    override def visit(n: SuperExpr, block: CFGStatements): Option[IRExpression] = {
        // TODO: more specific
        IRSuper(n)
    }

    override def visit(n: ThisExpr, block: CFGStatements): Option[IRExpression] = {
        // TODO: more specific
        IRThis(n)
    }

    override def visit(n: UnaryExpr, block: CFGStatements): Option[IRExpression] = {
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
                val source = cfg.readVar(typeOf(n.getExpression).getOrElse("<Unknown>"), name, block)
                val ope = if (n.getOperator == UnaryExpr.Operator.PREFIX_INCREMENT || n.getOperator == UnaryExpr.Operator.POSTFIX_INCREMENT)
                    BinaryExpr.Operator.PLUS
                else
                    BinaryExpr.Operator.MINUS

                for (
                    ty <- typeOf(n.getExpression)
                ) yield {
                    val target = block.addStatement(new IRBinaryOperation(cfg, ty, ope, source, IRInteger(1, n), Set(n))).target
                    cfg.writeVar(name, block, target)
                    if (n.getOperator == UnaryExpr.Operator.PREFIX_INCREMENT || n.getOperator == UnaryExpr.Operator.PREFIX_DECREMENT)
                        target
                    else
                        source
                }
            case ope =>
                for (
                    source <- n.getExpression.accept(this, block);
                    ty <- typeOf(n.getExpression)
                ) yield {
                    block.addStatement(new IRUnaryOperation(cfg, ty, ope, source, Set(n))).target
                }
        }
    }

    override def visit(n: VariableDeclarationExpr, block: CFGStatements): Option[IRExpression] = {
        n.getVariables.asScala.foreach(_.accept(this, block))
        null // TODO
    }

    override def visit(n: TypeExpr, block: CFGStatements): Option[IRExpression] = {
        IRTypeObject(n.getType.asString(), n)
    }

    override def visit(n: VariableDeclarator, block: CFGStatements): Option[IRExpression] = {
        for (
            initValue <- n.getInitializer.asScala.map(_.accept(this, block)).getOrElse(Some(IRNull(n)));
            ty <- Try(CodeUtil.resolvedTypeToType(n.getType.resolve()).toString).toOption
        ) yield {
            val name = n.getName.asString()
            initValue match {
                case t: IRTemp =>
                    cfg.writeVar(name, block, t)
                    t
                case _ =>
                    val target = block.addStatement(new IRAssignment(cfg, ty, initValue, Set(n))).target
                    cfg.writeVar(name, block, target)
                    target
            }
        }

    }

    implicit def wrapOption[A](value: A): Option[A] = Some(value)
}
