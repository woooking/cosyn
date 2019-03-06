package com.github.woooking.cosyn.pattern.dfgprocessor

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.{MethodDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.stmt._
import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.pattern.api.CodeGenerator
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.util.GraphTypeDef
import com.github.woooking.cosyn.pattern.util.OptionConverters._
import com.github.woooking.cosyn.skeleton.model
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{BasicType, FindNameContext, HoleExpr, Type}
import com.github.woooking.cosyn.util.CodeUtil
import org.slf4s.Logging

import scala.annotation.tailrec
import scala.collection.JavaConverters._

class DFG2Pattern extends CodeGenerator[DFGNode, DFGEdge, SimpleDFG, Pattern] with GraphTypeDef[DFGNode, DFGEdge] with Logging {

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

    implicit val findNameContext = new SimpleFindNameContext

    override def generate(originalGraph: Seq[SimpleDFG])(graph: PGraph): Pattern = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(graph)).filter(_._2._1).head
        generateCode(dfg, nodes)
    }

    def generateCode(dfg: SimpleDFG, nodes: Set[PNode]): Pattern = {
        val recoverNodes = dfg.recover(nodes)
        val block = generateCode(dfg.cfg.decl, recoverNodes, Set.empty)
        Pattern(block)
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
                (added :+ expr2stmt(expr), ctx)
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
            //            case n: ReturnStmt if n.getExpression.isEmpty =>
            //                rs(s"${indent}return;", names)
            //            case n: ReturnStmt =>
            //                val (code, ctx) = gc1(n.getExpression.get(), "")
            //                rs(s"return $code;", ctx, code)
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
                (null, names)
        }
    }

    def generateCodeExpr(node: Expression, nodes: Set[Node], names: Set[String], noName: Boolean = false): GenExprResult = {

        @inline
        def rs0(gen: => model.Expression, ctx: Set[String]): GenExprResult = {
            if (nodes.contains(node)) GenExprResult(gen, ctx, Nil)
            else GenExprResult(HoleExpr(), ctx, Nil)
        }

        @inline
        def rs1(gen: model.Expression => model.Expression, ctx: Set[String], ast1: Expression): GenExprResult = {
            val GenExprResult(e, ctx, added) = generateCodeExpr(ast1, nodes, names)
            if (nodes.contains(node)) GenExprResult(gen(e), ctx, added)
            else GenExprResult(HoleExpr(), ctx, added)
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
            //            case n: BooleanLiteralExpr =>
            //                if (nodes.contains(node)) (s"${n.getValue}", names)
            //                else ("", names)
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
            //            case n: FieldAccessExpr =>
            //                val (code, ctx) = gc1(n.getScope, "")
            //                if (nodes.contains(node)) (s"${el(code)}.${n.getName}", ctx)
            //                else rsn(ctx, code)
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
                val receiverType = BasicType(scope.calculateResolvedType().toString)

                def process(remain: List[Expression], ctx: Ctx, added: List[model.Statement], currentArgs: List[model.MethodCallArgs]): (List[model.MethodCallArgs], Ctx, List[model.Statement]) = {
                    remain match {
                        case Nil =>
                            (currentArgs, ctx, added)
                        case h :: tail =>
                            val resolvedType = h.calculateResolvedType()
                            val ty = CodeUtil.resolvedTypeToType(resolvedType)
                            val GenExprResult(e, newCtx, newAdded) = generateCodeExpr(h, nodes, ctx)
                            val n = name(ty)
                            process(tail, newCtx, (added ++ newAdded) :+ expr2stmt(v(ty, n, e)), currentArgs :+ model.MethodCallArgs(ty, n))
                    }
                }

                val GenExprResult(scopeCode, ctx, added) = generateCodeExpr(scope, nodes, names)

                val (args, ctx2, added2) = process(n.getArguments.asScala.toList, ctx, added, Nil)

                if (nodes.contains(node)) {
                    GenExprResult(call(scopeCode, receiverType, n.getName.asString(), args: _*), ctx2, added)
                } else {
                    GenExprResult(HoleExpr(), ctx2, added2)
                }

            case n: NameExpr =>
                val name = n.getName.asString()
                if (nodes.contains(node)) GenExprResult(name, names, Nil)
                else if (noName) GenExprResult(HoleExpr(), names, Nil)
                else if (names.contains(name)) GenExprResult(name, names, Nil)
                else GenExprResult(HoleExpr(), names, Nil)
            //            case _: NullLiteralExpr => rs("null", names)
            //            case n: ObjectCreationExpr=>
            //                val (argsCode, ctx) = gcl(n.getArguments, "")
            //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
            //                if (nodes.contains(node)) (s"${indent}new ${n.getType}($ellip)", ctx)
            //                else rsn(ctx, argsCode: _*)
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
            //            case n: UnaryExpr =>
            //                val ope = n.getOperator
            //                val (code, ctx) = gc1(n.getExpression, "")
            //                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
            //                rs(c, ctx, code)
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
        if (!nodes.contains(node)) return GenExprResult(HoleExpr(), names, Nil)
        val ty = CodeUtil.jpTypeToType(node.getType)
        node.getInitializer.asScala match {
            case Some(value) =>
                val GenExprResult(expr, ctx2, added) = generateCodeExpr(value, nodes, names)
                GenExprResult(v(ty, node.getName.asString(), expr), ctx2 + node.getName.asString(), added)
            case None =>
                GenExprResult(v(ty, node.getName.asString()), names + node.getName.asString(), Nil)
        }
    }

}
