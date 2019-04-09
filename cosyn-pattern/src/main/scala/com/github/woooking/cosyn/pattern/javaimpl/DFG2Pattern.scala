package com.github.woooking.cosyn.pattern.javaimpl

import com.github.javaparser.ast.{ArrayCreationLevel, Node}
import com.github.javaparser.ast.body.{MethodDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.stmt._
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder.{name, _}
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, FindNameContext, HoleExpr, HoleFactory, NameOrHole, Type}
import com.github.woooking.cosyn.comm.skeleton.{Pattern, model}
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.pattern.Components
import com.github.woooking.cosyn.pattern.api.PatternGenerator
import com.github.woooking.cosyn.pattern.javaimpl.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.util.GraphTypeDef
import org.slf4s.Logging

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._
import scala.util.{Failure, Success, Try}

class DFG2Pattern extends PatternGenerator[DFGNode, DFGEdge, SimpleDFG, Pattern] with GraphTypeDef[DFGNode, DFGEdge] with Logging {
    private val methodEntityRepository = Components.methodEntityRepository

    val holeFactory = HoleFactory()

    class SimpleFindNameContext extends FindNameContext {

        import scala.collection.mutable

        val numbering: mutable.Map[Type, Int] = mutable.Map[Type, Int]()

        override def nextIdForType(ty: Type): Int = {
            val id = numbering.getOrElseUpdate(ty, 1)
            numbering(ty) = id + 1
            id
        }
    }

    type Ctx = Map[String, model.NameExpr]

    case class GenExprResult(e: model.Expression, ctx: Ctx, added: List[model.Statement])

    private implicit val findNameContext: SimpleFindNameContext = new SimpleFindNameContext

    override def generate(originalGraph: Seq[SimpleDFG])(graph: PGraph): Pattern = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(graph)).filter(_._2._1).head
        generateCode(dfg, nodes)
    }

    def generateCode(dfg: SimpleDFG, nodes: Set[PNode]): Pattern = {
        val recoverNodes = dfg.recover(nodes)
        val block = generateCode(dfg.cfg.decl, recoverNodes, Map.empty)
        Pattern(holeFactory, block)
    }

    def generateCode(node: Node, nodes: Set[Node], names: Ctx): model.BlockStmt = node match {
        case n: MethodDeclaration if n.getBody.isPresent =>
            val (stmts, _) = generateCodeStmt(n.getBody.get(), nodes, names)
            block(stmts: _*)
        case _ =>
            log.warn(s"not implemented ${node.getClass} $node")
            ???
    }

    def generateCodeStmt(node: Statement, nodes: Set[Node], names: Ctx): (List[model.Statement], Ctx) = {
        node match {
            case n: BlockStmt =>
                @tailrec
                def process(remain: List[Statement], ctx: Ctx, result: List[model.Statement]): (List[model.Statement], Ctx) = remain match {
                    case Nil =>
                        (result, ctx)
                    case h :: tail =>
                        val (s, newCtx) = generateCodeStmt(h, nodes, ctx)
                        process(tail, newCtx, result ++ s)
                }

                process(n.getStatements.asScala.toList, names, Nil)
            //            case n: ConstructorDeclaration => generateCode(n.getBody, nodes, names, indent)
            //            case n: EnclosedExpr => generateCode(n.getInner, nodes, names, indent)
            case n: ExpressionStmt =>
                val GenExprResult(expr, ctx, added) = generateCodeExpr(n.getExpression, nodes, names)
                if (expr.isInstanceOf[HoleExpr]) (added, ctx)
                else (added :+ expr2stmt(expr), ctx)
            case n: ForEachStmt =>
                val GenExprResult(iterable, ctx, added) = generateCodeExpr(n.getIterable, nodes, names)
                val (body, ctx2) = generateCodeStmt(n.getBody, nodes, ctx)
                if (nodes.contains(node)) (added :+ foreach(CodeUtil.jpTypeToType(n.getVariableDeclarator.getType), n.getVariableDeclarator.getName.asString(), iterable, block(body: _*)), ctx2)
                else (added ++ body, ctx2)

            case n: ForStmt if n.getCompare.isEmpty =>
                @tailrec
                def process(exprs: List[Expression], ctx: Ctx, added: List[model.Statement], result: List[model.Expression]): (List[model.Expression], Ctx, List[model.Statement]) = exprs match {
                    case Nil => (result.reverse, ctx, added)
                    case h :: t =>
                        val GenExprResult(init, newCtx, newAdded) = generateCodeExpr(h, nodes, ctx)
                        process(t, newCtx, added ++ newAdded, init :: result)
                }

                val (init, ctx1, added1) = process(n.getInitialization.asScala.toList, names, Nil, Nil)
                val (update, ctx2, added2) = process(n.getUpdate.asScala.toList, ctx1, Nil, Nil)

                val (body, ctx3) = generateCodeStmt(n.getBody, nodes, ctx2)

                if (nodes.contains(node)) (added1 ++ added2 :+ forStmt(init, update, block(body: _*)), ctx3)
                else (added1 ++ added2 ++ body, ctx3)
            case n: ForStmt =>
                @tailrec
                def process(exprs: List[Expression], ctx: Ctx, added: List[model.Statement], result: List[model.Expression]): (List[model.Expression], Ctx, List[model.Statement]) = exprs match {
                    case Nil => (result.reverse, ctx, added)
                    case h :: t =>
                        val GenExprResult(init, newCtx, newAdded) = generateCodeExpr(h, nodes, ctx)
                        process(t, newCtx, added ++ newAdded, init :: result)
                }

                val (init, ctx1, added1) = process(n.getInitialization.asScala.toList, names, Nil, Nil)
                val GenExprResult(condition, ctx2, added2) = generateCodeExpr(n.getCompare.get(), nodes, ctx1)
                val (update, ctx3, added3) = process(n.getUpdate.asScala.toList, ctx2, Nil, Nil)

                val (body, ctx4) = generateCodeStmt(n.getBody, nodes, ctx3)

                if (nodes.contains(node)) (added1 ++ added2 ++ added3 :+ forStmt(init, condition, update, block(body: _*)), ctx4)
                else (added1 ++ added2 ++ added3 ++ body, ctx4)

            case n: IfStmt if n.getElseStmt.isEmpty =>
                val GenExprResult(condition, ctx, added) = generateCodeExpr(n.getCondition, nodes, names)
                val (thenStmt, ctx2) = generateCodeStmt(n.getThenStmt, nodes, ctx)
                if (nodes.contains(node)) (added :+ when(condition, block(thenStmt: _*)), ctx2)
                else (added ++ thenStmt, ctx2)
            case n: IfStmt =>
                val GenExprResult(condition, ctx, added) = generateCodeExpr(n.getCondition, nodes, names)
                val (thenStmt, ctx2) = generateCodeStmt(n.getThenStmt, nodes, ctx)
                val (elseStmt, ctx3) = generateCodeStmt(n.getElseStmt.get(), nodes, ctx2)
                if (nodes.contains(node)) (added :+ when(condition, block(thenStmt: _*), block(elseStmt: _*)), ctx3)
                else (added ++ thenStmt ++ elseStmt, ctx3)
            case n: ReturnStmt if n.getExpression.isEmpty =>
                if (nodes.contains(node)) (ret() :: Nil, names)
                else (Nil, names)
            case n: ReturnStmt =>
                val GenExprResult(expr, ctx, added) = generateCodeExpr(n.getExpression.get(), nodes, names)
                if (nodes.contains(node)) (added :+ ret(expr), ctx)
                else (added, ctx)
            //            case n: ThrowStmt =>
            //                val (code, ctx) = gc1(n.getExpression, "")
            //                if (nodes.contains(node)) (s"throw $code", ctx)
            //                else rsn(ctx, code)
            case n: WhileStmt =>
                val GenExprResult(condition, ctx1, added1) = generateCodeExpr(n.getCondition, nodes, names)
                val (body, ctx2) = generateCodeStmt(n.getBody, nodes, ctx1)
                if (nodes.contains(node)) (added1 :+ whileStmt(condition, block(body: _*)), ctx2)
                else (added1 ++ body, ctx2)
            case n: TryStmt =>
                generateCodeStmt(n.getTryBlock, nodes, names)
            case _ =>
                log.warn(s"not implemented ${node.getClass} $node")
                ???
        }
    }

    def generateCodeExpr(node: Expression, nodes: Set[Node], names: Ctx): GenExprResult = {
        @tailrec
        def processParams(remain: List[(String, Expression)], ctx: Ctx, added: List[model.Statement], currentArgs: List[model.MethodCallArgs]): (List[model.MethodCallArgs], Ctx, List[model.Statement]) = {
            remain match {
                case Nil =>
                    (currentArgs, ctx, added)
                case h :: tail =>
                    val ty = h._1
                    val expr = h._2
                    val GenExprResult(e, newCtx, newAdded) = generateCodeExpr(expr, nodes, ctx)
                    e match {
                        case _: model.HoleExpr | _: model.NameExpr =>
                            processParams(tail, newCtx, added ++ newAdded, currentArgs :+ model.MethodCallArgs(ty, e))
                        case _ =>
                            val n = name(ty)
                            processParams(tail, newCtx, (added ++ newAdded) :+ expr2stmt(v(ty, n, e)), currentArgs :+ model.MethodCallArgs(ty, n))
                    }
            }
        }

        @inline
        def rs0(gen: => model.Expression, ctx: Ctx): GenExprResult = {
            if (nodes.contains(node)) GenExprResult(gen, ctx, Nil)
            else GenExprResult(holeFactory.newHole(), ctx, Nil)
        }

        @inline
        def rs1(gen: model.Expression => model.Expression, ctx: Ctx, ast1: Expression): GenExprResult = {
            val GenExprResult(e, ctx1, added) = generateCodeExpr(ast1, nodes, ctx)
            if (nodes.contains(node)) GenExprResult(gen(e), ctx1, added)
            else GenExprResult(holeFactory.newHole(), ctx1, added)
        }

        @inline
        def rs2(gen: (model.Expression, model.Expression) => model.Expression, ctx: Ctx, ast1: Expression, ast2: Expression): GenExprResult = {
            val GenExprResult(e1, ctx1, added1) = generateCodeExpr(ast1, nodes, ctx)
            val GenExprResult(e2, ctx2, added2) = generateCodeExpr(ast2, nodes, ctx1)
            if (nodes.contains(node)) GenExprResult(gen(e1, e2), ctx2, added1 ++ added2)
            else GenExprResult(holeFactory.newHole(), ctx2, added1 ++ added2)
        }

        node match {
            case n: AssignExpr if n.getTarget.isNameExpr =>
                rs1(e => assign(n.getTarget.asNameExpr().getName.asString(), e), names, n.getValue)
            //            case n: AssignExpr =>
            //                val (targetCode, valueCode, ctx) = gc2(n.getTarget, n.getValue, "")
            //                rs(s"$targetCode${n.getOperator.asString()}$valueCode", ctx, targetCode, valueCode)
            //            case n: ArrayAccessExpr =>
            //                val (nameCode, indexCode, ctx) = gc2(n.getName, n.getIndex, "")
            //                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
            case _: ArrayCreationExpr =>
                //                @tailrec
                //                def processDims(remain: List[ArrayCreationLevel], ctx: Ctx, added: List[model.Statement], currentDims: List[Option[model.Expression]]): (List[Option[model.Expression]], Ctx, List[model.Statement]) = {
                //                    remain match {
                //                        case Nil =>
                //                            (currentDims, ctx, added)
                //                        case h :: tail =>
                //                            h.getDimension.asScala match {
                //                                case Some(value) =>
                //                                    val GenExprResult(e, newCtx, newAdded) = generateCodeExpr(value, nodes, ctx)
                //                                    processDims(tail, newCtx, added ++ newAdded, currentDims :+ Some(e))
                //                                case None =>
                //                                    processDims(tail, ctx, added, currentDims :+ None)
                //                            }
                //
                //                    }
                //                }

                GenExprResult(holeFactory.newHole(), names, Nil)
            //            case n: ArrayInitializerExpr =>
            //                val (valuesCode, ctx) = gcl(n.getValues, "")
            //                val ellip = valuesCode.map(el).mkString(", ")
            //                rs(s"{$ellip}", ctx, valuesCode: _*)
            case n: BinaryExpr =>
                rs2((left, right) => binary(n.getOperator.asString(), left, right), names, n.getLeft, n.getRight)
            case n: BooleanLiteralExpr =>
                rs0(model.BooleanLiteral(n.getValue), names)
            case n: CastExpr =>
                generateCodeExpr(n.getExpression, nodes, names)
            //            case n: ClassExpr =>
            //                if (nodes.contains(node)) (s"${n.getType}.class", names)
            //                else ("", names)
            //            case n: ConditionalExpr =>
            //                val (conditionCode, thenCode, elseCode, ctx) = gc3(n.getCondition, n.getThenExpr, n.getElseExpr, "")
            //                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
            //            case n: ConstructorDeclaration => generateCode(n.getBody, nodes, names, indent)
            case n: EnclosedExpr => generateCodeExpr(n.getInner, nodes, names)
            case n: FieldAccessExpr if n.getScope.isNameExpr && n.getScope.asNameExpr().getName.asString().matches("^[A-Z].*") => // TODO: enum or static field access
                val constant: NameOrHole = if (nodes.contains(n.getName)) n.getName.asString() else holeFactory.newHole()
                val ty = CodeUtil.resolvedTypeToType(n.getScope.asNameExpr().calculateResolvedType()).asInstanceOf[BasicType]
                rs0(enum(ty, constant), names)
            case n: FieldAccessExpr =>
                val constant: NameOrHole = if (nodes.contains(n.getName)) n.getName.asString() else holeFactory.newHole()
                val GenExprResult(receiver, ctx, added) = generateCodeExpr(n.getScope, nodes, names)
                Try {
                    val ty = CodeUtil.resolvedTypeToType(n.getScope.calculateResolvedType()).asInstanceOf[BasicType]
                    GenExprResult(field(ty, receiver, constant), ctx, added)
                } match {
                    case Success(value) if nodes.contains(node) =>
                        value
                    case Failure(_) =>
                        GenExprResult(holeFactory.newHole(), ctx, added)
                }
            //            case n: InstanceOfExpr =>
            //                val (code, ctx) = gc1(n.getExpression, "")
            //                rs(s"$code instanceof ${n.getType}", ctx, code)
            case n: IntegerLiteralExpr =>
                rs0(model.IntLiteral(n.asInt()), names)
            case n: LongLiteralExpr =>
                rs0(model.LongLiteral(n.asLong()), names)
            //            case n: MethodCallExpr if n.getScope.isEmpty =>
            //                val (argsCode, ctx) = gcl(n.getArguments, "")
            //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
            //                if (nodes.contains(node)) (s"${n.getName}($ellip)", ctx)
            //                else rsn(ctx, argsCode: _*)
            case n: MethodCallExpr if n.getScope.isPresent && Try {n.resolve()}.toOption.exists(_.isStatic) =>
                Try {
                    val resolved = n.resolve()
                    val paramTypes = (0 until resolved.getNumberOfParams).map(resolved.getParam).map(_.describeType())
                    processParams((paramTypes zip n.getArguments.asScala).toList, names, Nil, Nil)
                } match {
                    case Failure(_: UnsolvedSymbolException) =>
                        GenExprResult(holeFactory.newHole(), names, Nil)
                    case Failure(exception) =>
                        exception.printStackTrace()
                        ???
                    case Success((args, ctx2, added2)) =>
                        Try {
                            val qualifiedSignature = methodEntityRepository.getMethodProto(n.resolve().getQualifiedSignature)
                            GenExprResult(call(CodeUtil.methodReceiverType(qualifiedSignature).get.ty, CodeUtil.methodReceiverType(qualifiedSignature).get, n.getName.asString(), args: _*), ctx2, added2)
                        } match {
                            case Success(value) if nodes.contains(node) =>
                                value
                            case _ =>
                                GenExprResult(holeFactory.newHole(), ctx2, added2)
                        }
                }
            case n: MethodCallExpr if n.getScope.isPresent =>
                val scope = n.getScope.get()

                val GenExprResult(scopeCode, ctx, added) = generateCodeExpr(scope, nodes, names)

                Try {
                    val resolved = n.resolve()
                    val paramTypes = (0 until resolved.getNumberOfParams).map(resolved.getParam).map(_.describeType())
                    processParams((paramTypes zip n.getArguments.asScala).toList, ctx, added, Nil)
                } match {
                    case Failure(_: UnsolvedSymbolException) =>
                        GenExprResult(holeFactory.newHole(), ctx, added)
                    case Failure(exception) =>
                        exception.printStackTrace()
                        ???
                    case Success((args, ctx2, added2)) =>
                        Try {
                            val qualifiedSignature = methodEntityRepository.getMethodProto(n.resolve().getQualifiedSignature)
                            GenExprResult(call(scopeCode, CodeUtil.methodReceiverType(qualifiedSignature).get, n.getName.asString(), args: _*), ctx2, added2)
                        } match {
                            case Success(value) if nodes.contains(node) =>
                                value
                            case _ =>
                                GenExprResult(holeFactory.newHole(), ctx2, added2)
                        }
                }
            case n: NameExpr =>
                val name = n.getName.asString()
                if (nodes.contains(n)) GenExprResult(name, names, Nil)
                else if (names.contains(name)) GenExprResult(names(name), names, Nil)
                else GenExprResult(holeFactory.newHole(), names, Nil)
            case _: NullLiteralExpr => rs0(model.NullLiteral, names)
            case n: ObjectCreationExpr if n.getScope.isEmpty =>
                val receiverType = CodeUtil.resolvedTypeToType(n.getType.resolve()).asInstanceOf[BasicType]

                val resolved = n.resolve()
                val paramTypes = (0 until resolved.getNumberOfParams).map(resolved.getParam).map(_.describeType())
                val (args, ctx, added) = processParams((paramTypes zip n.getArguments.asScala).toList, names, Nil, Nil)

                if (nodes.contains(node)) {
                    GenExprResult(create(receiverType, args: _*), ctx, added)
                } else {
                    GenExprResult(holeFactory.newHole(), ctx, added)
                }
            case n: StringLiteralExpr =>
                rs0(model.StringLiteral(n.asString()), names)
            //            case _: ThisExpr => rs("this", names)
            case n: UnaryExpr =>
                val ope = n.getOperator
                rs1(e => unary(e, ope.asString(), ope.isPrefix), names, n.getExpression)
            case n: VariableDeclarationExpr =>
                generateVariableDeclarator(n.getVariable(0), nodes, names)
            //            case n: VariableDeclarator if n.getInitializer.isEmpty =>
            //                rs(s"${n.getType} ${n.getName}", names)
            case _ =>
                log.warn(s"not implemented ${node.getClass} $node")
                ???
        }
    }

    def generateVariableDeclarator(node: VariableDeclarator, nodes: Set[Node], names: Ctx): GenExprResult = {
        try {
            val ty = CodeUtil.resolvedTypeToType(node.getType.resolve())
            node.getInitializer.asScala match {
                case Some(value) =>
                    val GenExprResult(expr, ctx2, added) = generateCodeExpr(value, nodes, names)
                    expr match {
                        case _: HoleExpr =>
                            GenExprResult(holeFactory.newHole(), names, Nil)
                        case _ =>
                            val newName = name(ty)
                            GenExprResult(v(ty, newName, expr), ctx2 + (node.getName.asString() -> newName), added)
                    }
                case None =>
                    GenExprResult(holeFactory.newHole(), names, Nil)
            }
        } catch {
            case _: UnsolvedSymbolException =>
                GenExprResult(holeFactory.newHole(), names, Nil)
        }

    }

}