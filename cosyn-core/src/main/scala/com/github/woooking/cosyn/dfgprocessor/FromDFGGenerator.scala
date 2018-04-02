package com.github.woooking.cosyn.dfgprocessor

import com.github.woooking.cosyn.CodeGenerator
import com.github.woooking.cosyn.dfgprocessor.dfg.{SimpleDFG, DFGEdge, DFGNode}
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.woooking.cosyn.javaparser.body.{ConstructorDeclaration, MethodDeclaration, VariableDeclarator}
import com.github.woooking.cosyn.javaparser.expr._
import com.github.woooking.cosyn.javaparser.stmt._
import com.github.woooking.cosyn.util.GraphTypeDef
import de.parsemis.miner.general.Fragment

case class FromDFGGenerator() extends CodeGenerator[DFGNode, DFGEdge, SimpleDFG, String] with GraphTypeDef[DFGNode, DFGEdge] {
    override def generate(originalGraph: Seq[SimpleDFG])(fragment: Fragment[DFGNode, DFGEdge]): String = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(fragment.toGraph)).filter(_._2._1).head
        generateCode(dfg, nodes)
    }

    def generateCode(dfg: SimpleDFG, nodes: Set[PNode]): String = {
        val recoverNodes = dfg.recover(nodes)
        generateCode(dfg.cfg.decl, recoverNodes, Set.empty, "")._1
    }

    def generateCode(node: NodeDelegate[_], nodes: Set[NodeDelegate[_]], names: Set[String], indent: String, noName: Boolean = false): (String, Set[String]) = {
        @inline
        def el(c: String) = if (c == "") "..." else c

        @inline
        def gc1(n1: NodeDelegate[_], idt: String, ctx: Set[String] = names) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, !nodes.contains(node))
            (c1, ctx1)
        }

        @inline
        def gc2(n1: NodeDelegate[_], n2: NodeDelegate[_], idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, notVisitName)
            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt, notVisitName)
            (c1, c2, ctx2)
        }

        @inline
        def gc3(n1: NodeDelegate[_], n2: NodeDelegate[_], n3: NodeDelegate[_], idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt)
            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt)
            val (c3, ctx3) = generateCode(n3, nodes, ctx2, idt)
            (c1, c2, c3, ctx3)
        }

        @inline
        def gcl(ns: List[NodeDelegate[_]], idt: String, ctx: Set[String] = names) = {
            ns.foldLeft((Seq.empty[String], ctx))((context, v) => {
                val (c, ctx) = generateCode(v, nodes, context._2, idt, !nodes.contains(node))
                (context._1 :+ c, ctx)
            })
        }

        @inline
        def rs(code: => String, ctx: Set[String], cs: String*) = {
            if (nodes.contains(node) || cs.exists(_ != "")) (code, ctx)
            else ("", ctx)
        }

        @inline
        def rsn(ctx: Set[String], cs: String*) = {
            (cs.filter(_ != "").mkString("\n"), ctx)
        }

        node match {
            case AssignExpr(NameExpr(name), ope, value) =>
                val (valueCode, ctx) = gc1(value, "")
                val newCtx = if (nodes.contains(value)) ctx + name else ctx
                rs(s"$name $ope $valueCode", newCtx, valueCode)
            case AssignExpr(target, ope, value) =>
                val (targetCode, valueCode, ctx) = gc2(target, value, "")
                rs(s"$targetCode$ope$valueCode", ctx, targetCode, valueCode)
            case ArrayAccessExpr(name, index) =>
                val (nameCode, indexCode, ctx) = gc2(name, index, "")
                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
            case ArrayCreationExpr(ty, _, _) => (s"new $ty[]", names)
            case ArrayInitializerExpr(values) =>
                val (valuesCode, ctx) = gcl(values, "")
                val ellip = valuesCode.map(el).mkString(", ")
                rs(s"{$ellip}", ctx, valuesCode: _*)
            case BinaryExpr(left, ope, right) =>
                val (leftCode, rightCode, ctx) = gc2(left, right, "")
                if (nodes.contains(node)) (s"${el(leftCode)} ${ope.asString()} ${el(rightCode)}", ctx)
                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
                else (s"$leftCode$rightCode", ctx)
            case BlockStmt(s) =>
                val (codes, ctx) = gcl(s, indent)
                (codes.mkString(""), ctx)
            case BooleanLiteralExpr(s) =>
                if (nodes.contains(node)) (s"$s", names)
                else ("", names)
            case CastExpr(ty, expr) =>
                val (code, ctx) = gc1(expr, "")
                rs(s"($ty)$code", ctx, code)
            case ClassExpr(ty) =>
                if (nodes.contains(node)) (s"$ty.class", names)
                else ("", names)
            case ConditionalExpr(condition, thenExpr, elseExpr) =>
                val (conditionCode, thenCode, elseCode, ctx) = gc3(condition, thenExpr, elseExpr, "")
                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
            case ConstructorDeclaration(_, _, _, _, _, body) => generateCode(body, nodes, names, indent)
            case EnclosedExpr(e) => generateCode(e, nodes, names, indent)
            case ExpressionStmt(s) =>
                val (code, ctx) = gc1(s, "")
                if (code != "") (s"$indent$code;\n", ctx)
                else ("", ctx)
            case FieldAccessExpr(scope, name, _) =>
                val (code, ctx) = gc1(scope, "")
                if (nodes.contains(node)) (s"${el(code)}.$name", ctx)
                else rsn(ctx, code)
            case ForeachStmt(_, _, body) =>
                val (code, ctx) = gc1(body, s"$indent    ")
                if (nodes.contains(node)) (s"${indent}for () {\n$code$indent}\n", ctx)
                else rsn(ctx, code)
            case ForStmt(init, None, update, body) =>
                val (initCode, ctx1) = gcl(init, "")
                val (updateCode, ctx2) = gcl(update, "", ctx1)
                val (bodyCode, ctx3) = generateCode(body, nodes, ctx2, s"$indent    ")
                val codes = initCode ++ updateCode :+ bodyCode
                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
                else rsn(ctx3, codes: _*)
            case ForStmt(init, Some(compare), update, body) =>
                val (initCode, ctx1) = gcl(init, "")
                val (compareCode, ctx2) = generateCode(compare, nodes, ctx1, s"$indent    ")
                val (updateCode, ctx3) = gcl(update, "", ctx2)
                val (bodyCode, ctx4) = generateCode(body, nodes, ctx3, s"$indent    ")
                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
                else rsn(ctx4, codes: _*)
            case IfStmt(condition, thenStmt, None) =>
                val (conditionCode, ctx1) = generateCode(condition, nodes, names, "")
                val idt = if (nodes.contains(node)) s"$indent    " else indent
                val (thenCode, ctx2) = gc1(thenStmt, idt, ctx1)
                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
                else rsn(ctx2, conditionCode, thenCode)
            case IfStmt(condition, thenStmt, Some(elseStmt)) =>
                val (conditionCode, ctx1) = generateCode(condition, nodes, names, "")
                val idt = if (nodes.contains(node)) s"$indent    " else indent
                val (thenCode, elseCode, ctx2) = gc2(thenStmt, elseStmt, idt, ctx1)
                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
                else rsn(ctx2, conditionCode, thenCode, elseCode)
            case InstanceOfExpr(e, ty) =>
                val (code, ctx) = gc1(e, "")
                rs(s"$code instanceof $ty", ctx, code)
            case IntegerLiteralExpr(s) =>
                rs(s.toString, names)
            case LongLiteralExpr(s) =>
                rs(s.toString, names)
            case MethodCallExpr(name, None, _, args) =>
                val (argsCode, ctx) = gcl(args, "")
                val ellip = argsCode.map(c => if (c == "") "..." else c).mkString(", ")
                if (nodes.contains(node)) (s"$name($ellip)", ctx)
                else rsn(ctx, argsCode: _*)
            case MethodCallExpr(name, Some(scope), _, args) =>
                val (argsCode, ctx1) = gcl(args, "")
                val (scopeCode, ctx2) = gc1(scope, "", ctx1)
                val ellip = argsCode.map(c => if (c == "") "..." else c).mkString(", ")
                if (nodes.contains(node)) (s"${el(scopeCode)}.$name($ellip)", ctx2)
                else rsn(ctx2, (scopeCode +: argsCode): _*)
            case MethodDeclaration(_, _, _, _, _, _, Some(body)) => generateCode(body, nodes, names, indent)
            case NameExpr(name) =>
                if (nodes.contains(node)) (name, names)
                else if (noName) ("", names)
                else if (names.contains(name)) (name, names)
                else ("", names)
            case NullLiteralExpr() =>
                rs("null", names)
            case ObjectCreationExpr(_, ty, _, args, _) =>
                val (argsCode, ctx) = gcl(args, "")
                val ellip = argsCode.map(c => if (c == "") "..." else c).mkString(", ")
                if (nodes.contains(node)) (s"${indent}new $ty($ellip)", ctx)
                else rsn(ctx, argsCode: _*)
            case ReturnStmt(None) =>
                rs(s"${indent}return;", names)
            case ReturnStmt(Some(expr)) =>
                val (code, ctx) = gc1(expr, "")
                rs(s"return $code;", ctx, code)
            case StringLiteralExpr(s) =>
                rs(s.toString, names)
            case ThrowStmt(e) =>
                val (code, ctx) = gc1(e, "")
                if (nodes.contains(node)) (s"throw $code", ctx)
                else rsn(ctx, code)
            case ThisExpr(_) =>
                rs("this", names)
            case UnaryExpr(ope, expr) =>
                val (code, ctx) = gc1(expr, "")
                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
                rs(c, ctx, code)
            case VariableDeclarationExpr(v) =>
                val (code, ctx) = gcl(v, indent)
                (code.mkString(""), ctx)
            case VariableDeclarator(name, ty, None) =>
                rs(s"$ty $name", names)
            case VariableDeclarator(name, ty, Some(init)) =>
                val (code, ctx) = gc1(init, "")
                val newCtx = if (nodes.contains(node) || nodes.contains(init)) ctx + name else ctx
                rs(s"$ty $name = $code", newCtx, code)
                if (nodes.contains(node) || nodes.contains(init)) (s"$ty $name = $code", ctx + name)
                else rsn(ctx, code)
            case WhileStmt(condition, body) =>
                val (conditionCode, ctx1) = gc1(condition, "")
                val (bodyCode, ctx2) = gc1(body, s"$indent    ", ctx1)
                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
            case TryStmt(_, body, _, _) =>
                val (code, ctx) = gc1(body, indent)
                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
                rs(s"${indent}$code", ctx, code)
            case _ =>
                (s"not implemented $node", names)
        }
    }
}
