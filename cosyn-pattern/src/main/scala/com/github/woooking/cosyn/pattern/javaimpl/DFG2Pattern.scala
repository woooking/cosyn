package com.github.woooking.cosyn.pattern.javaimpl

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.{MethodDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.stmt._
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder.{name, _}
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, FindNameContext, HoleExpr, HoleFactory, NameOrHole, Type}
import com.github.woooking.cosyn.comm.skeleton.{Pattern, model}
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.pattern.api.PatternGenerator
import com.github.woooking.cosyn.pattern.javaimpl.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.util.GraphTypeDef
import scala.compat.java8.OptionConverters._
import org.slf4s.Logging

import scala.annotation.tailrec
import scala.collection.JavaConverters._

class DFG2Pattern extends PatternGenerator[DFGNode, DFGEdge, SimpleDFG, Pattern] with GraphTypeDef[DFGNode, DFGEdge] with Logging {
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

    type Ctx = Set[String]

    case class GenExprResult(e: model.Expression, ctx: Ctx, added: List[model.Statement])

    private implicit val findNameContext: SimpleFindNameContext = new SimpleFindNameContext

    override def generate(originalGraph: Seq[SimpleDFG])(graph: PGraph): Pattern = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(graph)).filter(_._2._1).head
        generateCode(dfg, nodes)
    }

    def generateCode(dfg: SimpleDFG, nodes: Set[PNode]): Pattern = {
        val recoverNodes = dfg.recover(nodes)
        val block = generateCode(dfg.cfg.decl, recoverNodes, Set.empty)
        Pattern(holeFactory, block)
    }

    def generateCode(node: Node, nodes: Set[Node], names: Ctx, noName: Boolean = false): model.BlockStmt = node match {
        case n: MethodDeclaration if n.getBody.isPresent =>
            val (stmts, _) = generateCodeStmt(n.getBody.get(), nodes, names)
            block(stmts: _*)
        case _ =>
            log.warn(s"not implemented ${node.getClass} $node")
            ???
    }

    def generateCodeStmt(node: Statement, nodes: Set[Node], names: Ctx, noName: Boolean = false): (List[model.Statement], Ctx) = {
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

            //            case n: ForStmt if n.getCompare.isEmpty =>
            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
            //                val (updateCode, ctx2) = gcl(n.getUpdate, "", ctx1)
            //                val (bodyCode, ctx3) = generateCode(n.getBody, nodes, ctx2, s"$indent    ")
            //                val codes = initCode ++ updateCode :+ bodyCode
            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
            //                else rsn(ctx3, codes: _*)
            //            case n: ForStmt =>
            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
            //                val (compareCode, ctx2) = generateCode(n.getCompare.get(), nodes, ctx1, s"$indent    ")
            //                val (updateCode, ctx3) = gcl(n.getUpdate, "", ctx2)
            //                val (bodyCode, ctx4) = generateCode(n.getBody, nodes, ctx3, s"$indent    ")
            //                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
            //                else rsn(ctx4, codes: _*)
            //            case n: IfStmt if n.getElseStmt.isEmpty =>
            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
            //                val (thenCode, ctx2) = gc1(n.getThenStmt, idt, ctx1)
            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
            //                else rsn(ctx2, conditionCode, thenCode)
            //            case n: IfStmt =>
            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
            //                val (thenCode, elseCode, ctx2) = gc2(n.getThenStmt, n.getElseStmt.get(), idt, ctx1)
            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
            //                else rsn(ctx2, conditionCode, thenCode, elseCode)
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
            //            case n: WhileStmt =>
            //                val (conditionCode, ctx1) = gc1(n.getCondition, "")
            //                val (bodyCode, ctx2) = gc1(n.getBody, s"$indent    ", ctx1)
            //                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
            case n: TryStmt =>
                generateCodeStmt(n.getTryBlock, nodes, names)
            case _ =>
                log.warn(s"not implemented ${node.getClass} $node")
                ???
        }
    }

    def generateCodeExpr(node: Expression, nodes: Set[Node], names: Set[String], noName: Boolean = false): GenExprResult = {

        @inline
        def rs0(gen: => model.Expression, ctx: Set[String]): GenExprResult = {
            if (nodes.contains(node)) GenExprResult(gen, ctx, Nil)
            else GenExprResult(holeFactory.newHole(), ctx, Nil)
        }

        @inline
        def rs1(gen: model.Expression => model.Expression, ctx: Set[String], ast1: Expression): GenExprResult = {
            val GenExprResult(e, ctx, added) = generateCodeExpr(ast1, nodes, names)
            if (nodes.contains(node)) GenExprResult(gen(e), ctx, added)
            else GenExprResult(holeFactory.newHole(), ctx, added)
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
            //            case n: ArrayCreationExpr => (s"new ${n.getElementType}[]", names)
            //            case n: ArrayInitializerExpr =>
            //                val (valuesCode, ctx) = gcl(n.getValues, "")
            //                val ellip = valuesCode.map(el).mkString(", ")
            //                rs(s"{$ellip}", ctx, valuesCode: _*)
            //            case n: BinaryExpr =>
            //                val (leftCode, rightCode, ctx) = gc2(n.getLeft, n.getRight, "")
            //                if (nodes.contains(node)) (s"${el(leftCode)} ${n.getOperator.asString()} ${el(rightCode)}", ctx)
            //                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
            //                else (s"$leftCode$rightCode", ctx)
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
            //            case n: EnclosedExpr => generateCode(n.getInner, nodes, names, indent)
            case n: FieldAccessExpr if n.getScope.isNameExpr && n.getScope.asNameExpr().getName.asString().matches("^[A-Z].*") =>
                val constant: NameOrHole = if (nodes.contains(n.getName)) n.getName.asString() else holeFactory.newHole()
                val ty = CodeUtil.resolvedTypeToType(n.getScope.asNameExpr().calculateResolvedType()).asInstanceOf[BasicType]
                rs0(enum(ty, constant), names)
            //            case n: ForEachStmt =>
            //                val (code, ctx) = gc1(n.getBody, s"$indent    ")
            //                if (nodes.contains(node)) (s"${indent}for () {\n$code$indent}\n", ctx)
            //                else rsn(ctx, code)
            //            case n: ForStmt if n.getCompare.isEmpty =>
            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
            //                val (updateCode, ctx2) = gcl(n.getUpdate, "", ctx1)
            //                val (bodyCode, ctx3) = generateCode(n.getBody, nodes, ctx2, s"$indent    ")
            //                val codes = initCode ++ updateCode :+ bodyCode
            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
            //                else rsn(ctx3, codes: _*)
            //            case n: ForStmt =>
            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
            //                val (compareCode, ctx2) = generateCode(n.getCompare.get(), nodes, ctx1, s"$indent    ")
            //                val (updateCode, ctx3) = gcl(n.getUpdate, "", ctx2)
            //                val (bodyCode, ctx4) = generateCode(n.getBody, nodes, ctx3, s"$indent    ")
            //                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
            //                else rsn(ctx4, codes: _*)
            //            case n: IfStmt if n.getElseStmt.isEmpty =>
            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
            //                val (thenCode, ctx2) = gc1(n.getThenStmt, idt, ctx1)
            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
            //                else rsn(ctx2, conditionCode, thenCode)
            //            case n: IfStmt =>
            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
            //                val (thenCode, elseCode, ctx2) = gc2(n.getThenStmt, n.getElseStmt.get(), idt, ctx1)
            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
            //                else rsn(ctx2, conditionCode, thenCode, elseCode)
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
            case n: MethodCallExpr if n.getScope.isPresent =>
                val scope = n.getScope.get()
                val receiverType = CodeUtil.resolvedTypeToType(scope.calculateResolvedType()).asInstanceOf[BasicType]

                def process(remain: List[Expression], ctx: Ctx, added: List[model.Statement], currentArgs: List[model.MethodCallArgs]): (List[model.MethodCallArgs], Ctx, List[model.Statement]) = {
                    remain match {
                        case Nil =>
                            (currentArgs, ctx, added)
                        case h :: tail =>
                            val resolvedType = h.calculateResolvedType()
                            val ty = CodeUtil.resolvedTypeToType(resolvedType)
                            val GenExprResult(e, newCtx, newAdded) = generateCodeExpr(h, nodes, ctx)
                            e match {
                                case _: HoleExpr =>
                                    process(tail, newCtx, added ++ newAdded, currentArgs :+ model.MethodCallArgs(ty, e))
                                case _ =>
                                    val n = name(ty)
                                    process(tail, newCtx, (added ++ newAdded) :+ expr2stmt(v(ty, n, e)), currentArgs :+ model.MethodCallArgs(ty, n))
                            }
                    }
                }

                val GenExprResult(scopeCode, ctx, added) = generateCodeExpr(scope, nodes, names)

                val (args, ctx2, added2) = process(n.getArguments.asScala.toList, ctx, added, Nil)

                if (nodes.contains(node)) {
                    GenExprResult(call(scopeCode, receiverType, n.getName.asString(), args: _*), ctx2, added2)
                } else {
                    GenExprResult(holeFactory.newHole(), ctx2, added2)
                }
            case n: NameExpr =>
                val name = n.getName.asString()
                if (nodes.contains(node)) GenExprResult(name, names, Nil)
                else if (noName) GenExprResult(holeFactory.newHole(), names, Nil)
                else if (names.contains(name)) GenExprResult(name, names, Nil)
                else GenExprResult(holeFactory.newHole(), names, Nil)
            //            case _: NullLiteralExpr => rs("null", names)
            case n: ObjectCreationExpr if n.getScope.isEmpty =>
                val receiverType = BasicType(n.getType.toString)

                def process(remain: List[Expression], ctx: Ctx, added: List[model.Statement], currentArgs: List[model.MethodCallArgs]): (List[model.MethodCallArgs], Ctx, List[model.Statement]) = {
                    remain match {
                        case Nil =>
                            (currentArgs, ctx, added)
                        case h :: tail =>
                            val resolvedType = h.calculateResolvedType()
                            val ty = CodeUtil.resolvedTypeToType(resolvedType)
                            val GenExprResult(e, newCtx, newAdded) = generateCodeExpr(h, nodes, ctx)
                            e match {
                                case _: HoleExpr =>
                                    process(tail, newCtx, added ++ newAdded, currentArgs :+ model.MethodCallArgs(ty, e))
                                case _ =>
                                    val n = name(ty)
                                    process(tail, newCtx, (added ++ newAdded) :+ expr2stmt(v(ty, n, e)), currentArgs :+ model.MethodCallArgs(ty, n))
                            }
                    }
                }

                val (args, ctx, added) = process(n.getArguments.asScala.toList, names, Nil, Nil)

                if (nodes.contains(node)) {
                    GenExprResult(create(receiverType, args: _*), ctx, added)
                } else {
                    GenExprResult(holeFactory.newHole(), ctx, added)
                }
            //            case n: ReturnStmt if n.getExpression.isEmpty =>
            //                rs(s"${indent}return;", names)
            //            case n: ReturnStmt =>
            //                val (code, ctx) = gc1(n.getExpression.get(), "")
            //                rs(s"return $code;", ctx, code)
            case n: StringLiteralExpr =>
                rs0(model.StringLiteral(n.asString()), names)
            //            case n: ThrowStmt =>
            //                val (code, ctx) = gc1(n.getExpression, "")
            //                if (nodes.contains(node)) (s"throw $code", ctx)
            //                else rsn(ctx, code)
            //            case _: ThisExpr => rs("this", names)
            case n: UnaryExpr =>
                val ope = n.getOperator
                rs1(e => unary(e, ope.asString(), ope.isPrefix), names, n.getExpression)
            case n: VariableDeclarationExpr =>
                generateVariableDeclarator(n.getVariable(0), nodes, names, noName)
            //            case n: VariableDeclarator if n.getInitializer.isEmpty =>
            //                rs(s"${n.getType} ${n.getName}", names)
            //            case n: VariableDeclarator =>
            //                val name = n.getName.asString()
            //                val (code, ctx) = gc1(n.getInitializer.get(), "")
            //                val newCtx = if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) ctx + name else ctx
            //                rs(s"${n.getType} $name = $code", newCtx, code)
            //                if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) (s"${n.getType} $name = $code", ctx + name)
            //                else rsn(ctx, code)
            //            case n: WhileStmt =>
            //                val (conditionCode, ctx1) = gc1(n.getCondition, "")
            //                val (bodyCode, ctx2) = gc1(n.getBody, s"$indent    ", ctx1)
            //                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
            //            case n: TryStmt =>
            //                val (code, ctx) = gc1(n.getTryBlock, indent)
            //                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
            //                rs(s"$indent$code", ctx, code)
            case _ =>
                log.warn(s"not implemented ${node.getClass} $node")
                ???
        }
    }

    def generateVariableDeclarator(node: VariableDeclarator, nodes: Set[Node], names: Set[String], noName: Boolean = false): GenExprResult = {
        val ty = CodeUtil.resolvedTypeToType(node.getType.resolve())
        node.getInitializer.asScala match {
            case Some(value) =>
                val GenExprResult(expr, ctx2, added) = generateCodeExpr(value, nodes, names)
                expr match {
                    case _: HoleExpr =>
                        if (!nodes.contains(node)) GenExprResult(holeFactory.newHole(), names, Nil)
                        else GenExprResult(v(ty, node.getName.asString()), names + node.getName.asString(), Nil)
                    case _ =>
                        GenExprResult(v(ty, node.getName.asString(), expr), ctx2 + node.getName.asString(), added)
                }
            case None =>
                if (!nodes.contains(node)) GenExprResult(holeFactory.newHole(), names, Nil)
                else GenExprResult(v(ty, node.getName.asString()), names + node.getName.asString(), Nil)
        }
    }

}
