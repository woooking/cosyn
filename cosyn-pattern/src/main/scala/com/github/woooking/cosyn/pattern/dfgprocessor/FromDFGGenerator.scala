package com.github.woooking.cosyn.pattern.dfgprocessor

import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, VariableDeclarator}
import com.github.javaparser.ast.{Node, NodeList}
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.stmt._
import com.github.woooking.cosyn.pattern.api.PatternGenerator
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.util.GraphTypeDef

import scala.collection.JavaConverters._

case class FromDFGGenerator() extends PatternGenerator[DFGNode, DFGEdge, SimpleDFG, String] with GraphTypeDef[DFGNode, DFGEdge] {
    override def generate(originalGraph: Seq[SimpleDFG])(graph: PGraph): String = {
        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(graph)).filter(_._2._1).head
        generateCode(dfg, nodes)
    }

    def generateCode(dfg: SimpleDFG, nodes: Set[PNode]): String = {
        val recoverNodes = dfg.recover(nodes)
        generateCode(dfg.cfg.decl, recoverNodes, Set.empty, "")._1
    }

    def generateCode(node: Node, nodes: Set[Node], names: Set[String], indent: String, noName: Boolean = false): (String, Set[String]) = {
        @inline
        def el(c: String) = if (c == "") "<HOLE>" else c

        @inline
        def gc1(n1: Node, idt: String, ctx: Set[String] = names) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, !nodes.contains(node))
            (c1, ctx1)
        }

        @inline
        def gc2(n1: Node, n2: Node, idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, notVisitName)
            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt, notVisitName)
            (c1, c2, ctx2)
        }

        @inline
        def gc3(n1: Node, n2: Node, n3: Node, idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt)
            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt)
            val (c3, ctx3) = generateCode(n3, nodes, ctx2, idt)
            (c1, c2, c3, ctx3)
        }

        @inline
        def gcl[T <: Node](ns: NodeList[T], idt: String, ctx: Set[String] = names) = {
            ns.asScala.foldLeft((Seq.empty[String], ctx))((context, v) => {
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
            case n: AssignExpr if n.getTarget.isNameExpr =>
                val name = n.getTarget.asNameExpr().getName.asString()
                val (valueCode, ctx) = gc1(n.getValue, "")
                val newCtx = if (nodes.contains(n.getValue)) ctx + name else ctx
                rs(s"$name ${n.getOperator.asString()} $valueCode", newCtx, valueCode)
            case n: AssignExpr =>
                val (targetCode, valueCode, ctx) = gc2(n.getTarget, n.getValue, "")
                rs(s"$targetCode${n.getOperator.asString()}$valueCode", ctx, targetCode, valueCode)
            case n: ArrayAccessExpr =>
                val (nameCode, indexCode, ctx) = gc2(n.getName, n.getIndex, "")
                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
            case n: ArrayCreationExpr => (s"new ${n.getElementType}[]", names)
            case n: ArrayInitializerExpr =>
                val (valuesCode, ctx) = gcl(n.getValues, "")
                val ellip = valuesCode.map(el).mkString(", ")
                rs(s"{$ellip}", ctx, valuesCode: _*)
            case n: BinaryExpr =>
                val (leftCode, rightCode, ctx) = gc2(n.getLeft, n.getRight, "")
                if (nodes.contains(node)) (s"${el(leftCode)} ${n.getOperator.asString()} ${el(rightCode)}", ctx)
                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
                else (s"$leftCode$rightCode", ctx)
            case n: BlockStmt =>
                val (codes, ctx) = gcl(n.getStatements, indent)
                (codes.mkString(""), ctx)
            case n: BooleanLiteralExpr =>
                if (nodes.contains(node)) (s"${n.getValue}", names)
                else ("", names)
            case n: CastExpr =>
                val (code, ctx) = gc1(n.getExpression, "")
                rs(s"(${n.getType})$code", ctx, code)
            case n: ClassExpr =>
                if (nodes.contains(node)) (s"${n.getType}.class", names)
                else ("", names)
            case n: ConditionalExpr =>
                val (conditionCode, thenCode, elseCode, ctx) = gc3(n.getCondition, n.getThenExpr, n.getElseExpr, "")
                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
            case n: ConstructorDeclaration => generateCode(n.getBody, nodes, names, indent)
            case n: EnclosedExpr => generateCode(n.getInner, nodes, names, indent)
            case n: ExpressionStmt =>
                val (code, ctx) = gc1(n.getExpression, "")
                if (code != "") (s"$indent$code;\n", ctx)
                else ("", ctx)
            case n: FieldAccessExpr =>
                val (code, ctx) = gc1(n.getScope, "")
                if (nodes.contains(node)) (s"${el(code)}.${n.getName}", ctx)
                else rsn(ctx, code)
            case n: ForEachStmt =>
                val (code, ctx) = gc1(n.getBody, s"$indent    ")
                if (nodes.contains(node)) (s"${indent}for () {\n$code$indent}\n", ctx)
                else rsn(ctx, code)
            case n: ForStmt if n.getCompare.isEmpty =>
                val (initCode, ctx1) = gcl(n.getInitialization, "")
                val (updateCode, ctx2) = gcl(n.getUpdate, "", ctx1)
                val (bodyCode, ctx3) = generateCode(n.getBody, nodes, ctx2, s"$indent    ")
                val codes = initCode ++ updateCode :+ bodyCode
                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
                else rsn(ctx3, codes: _*)
            case n: ForStmt =>
                val (initCode, ctx1) = gcl(n.getInitialization, "")
                val (compareCode, ctx2) = generateCode(n.getCompare.get(), nodes, ctx1, s"$indent    ")
                val (updateCode, ctx3) = gcl(n.getUpdate, "", ctx2)
                val (bodyCode, ctx4) = generateCode(n.getBody, nodes, ctx3, s"$indent    ")
                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
                else rsn(ctx4, codes: _*)
            case n: IfStmt if n.getElseStmt.isEmpty =>
                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
                val idt = if (nodes.contains(node)) s"$indent    " else indent
                val (thenCode, ctx2) = gc1(n.getThenStmt, idt, ctx1)
                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
                else rsn(ctx2, conditionCode, thenCode)
            case n: IfStmt =>
                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
                val idt = if (nodes.contains(node)) s"$indent    " else indent
                val (thenCode, elseCode, ctx2) = gc2(n.getThenStmt, n.getElseStmt.get(), idt, ctx1)
                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
                else rsn(ctx2, conditionCode, thenCode, elseCode)
            case n: InstanceOfExpr =>
                val (code, ctx) = gc1(n.getExpression, "")
                rs(s"$code instanceof ${n.getType}", ctx, code)
            case n: IntegerLiteralExpr =>
                rs(n.asInt().toString, names)
            case n: LongLiteralExpr =>
                rs(n.asLong().toString, names)
            case n: MethodCallExpr if n.getScope.isEmpty =>
                val (argsCode, ctx) = gcl(n.getArguments, "")
                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
                if (nodes.contains(node)) (s"${n.getName}($ellip)", ctx)
                else rsn(ctx, argsCode: _*)
            case n: MethodCallExpr =>
                val (argsCode, ctx1) = gcl(n.getArguments, "")
                val (scopeCode, ctx2) = gc1(n.getScope.get(), "", ctx1)
                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
                if (nodes.contains(node)) (s"${el(scopeCode)}.${n.getName}($ellip)", ctx2)
                else rsn(ctx2, scopeCode +: argsCode: _*)
            case n: MethodDeclaration if n.getBody.isPresent => generateCode(n.getBody.get(), nodes, names, indent)
            case n: NameExpr =>
                val name = n.getName.asString()
                if (nodes.contains(node)) (name, names)
                else if (noName) ("", names)
                else if (names.contains(name)) (name, names)
                else ("", names)
            case _: NullLiteralExpr => rs("null", names)
            case n: ObjectCreationExpr=>
                val (argsCode, ctx) = gcl(n.getArguments, "")
                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
                if (nodes.contains(node)) (s"${indent}new ${n.getType}($ellip)", ctx)
                else rsn(ctx, argsCode: _*)
            case n: ReturnStmt if n.getExpression.isEmpty =>
                rs(s"${indent}return;", names)
            case n: ReturnStmt =>
                val (code, ctx) = gc1(n.getExpression.get(), "")
                rs(s"return $code;", ctx, code)
            case n: StringLiteralExpr =>
                rs(n.asString(), names)
            case n: ThrowStmt =>
                val (code, ctx) = gc1(n.getExpression, "")
                if (nodes.contains(node)) (s"throw $code", ctx)
                else rsn(ctx, code)
            case _: ThisExpr => rs("this", names)
            case n: UnaryExpr =>
                val ope = n.getOperator
                val (code, ctx) = gc1(n.getExpression, "")
                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
                rs(c, ctx, code)
            case n: VariableDeclarationExpr =>
                val (code, ctx) = gcl(n.getVariables, indent)
                (code.mkString(""), ctx)
            case n: VariableDeclarator if n.getInitializer.isEmpty =>
                rs(s"${n.getType} ${n.getName}", names)
            case n: VariableDeclarator =>
                val name = n.getName.asString()
                val (code, ctx) = gc1(n.getInitializer.get(), "")
                val newCtx = if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) ctx + name else ctx
                rs(s"${n.getType} $name = $code", newCtx, code)
                if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) (s"${n.getType} $name = $code", ctx + name)
                else rsn(ctx, code)
            case n: WhileStmt =>
                val (conditionCode, ctx1) = gc1(n.getCondition, "")
                val (bodyCode, ctx2) = gc1(n.getBody, s"$indent    ", ctx1)
                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
            case n: TryStmt =>
                val (code, ctx) = gc1(n.getTryBlock, indent)
                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
                rs(s"$indent$code", ctx, code)
            case _ =>
                (s"not implemented $node", names)
        }
    }
}
