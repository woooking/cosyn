//package com.github.woooking.cosyn.pattern.dfgprocessor
//
//import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, VariableDeclarator}
//import com.github.javaparser.ast.expr._
//import com.github.javaparser.ast.stmt._
//import com.github.javaparser.ast.{Node, NodeList}
//import com.github.woooking.cosyn.Pattern
//import com.github.woooking.cosyn.pattern.api.CodeGenerator
//import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
//import com.github.woooking.cosyn.pattern.util.GraphTypeDef
//import org.slf4s.Logging
//
//import scala.collection.JavaConverters._
//import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
//import com.github.woooking.cosyn.skeleton.model
//import com.github.woooking.cosyn.skeleton.model.HoleExpr
//
//class DFG2Pattern extends CodeGenerator[DFGNode, DFGEdge, SimpleDFG, Pattern] with GraphTypeDef[DFGNode, DFGEdge] with Logging {
//    override def generate(originalGraph: Seq[SimpleDFG])(graph: PGraph): Pattern = {
//        val (dfg, (_, nodes)) = originalGraph.map(d => d -> d.isSuperGraph(graph)).filter(_._2._1).head
//        generateCode(dfg, nodes)
//    }
//
//    def generateCode(dfg: SimpleDFG, nodes: Set[PNode]): Pattern = {
//        val recoverNodes = dfg.recover(nodes)
//        val block = generateCode(dfg.cfg.decl, recoverNodes, Set.empty, "")
//        Pattern(block)
//    }
//
//    def generateCode(node: Node, nodes: Set[Node], names: Set[String], indent: String, noName: Boolean = false): model.BlockStmt = {
//        node match {
//            //            case n: AssignExpr if n.getTarget.isNameExpr =>
//            //                val name = n.getTarget.asNameExpr().getName.asString()
//            //                val (valueCode, ctx) = gc1(n.getValue, "")
//            //                val newCtx = if (nodes.contains(n.getValue)) ctx + name else ctx
//            //                rs(s"$name ${n.getOperator.asString()} $valueCode", newCtx, valueCode)
//            //            case n: AssignExpr =>
//            //                val (targetCode, valueCode, ctx) = gc2(n.getTarget, n.getValue, "")
//            //                rs(s"$targetCode${n.getOperator.asString()}$valueCode", ctx, targetCode, valueCode)
//            //            case n: ArrayAccessExpr =>
//            //                val (nameCode, indexCode, ctx) = gc2(n.getName, n.getIndex, "")
//            //                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
//            //            case n: ArrayCreationExpr => (s"new ${n.getElementType}[]", names)
//            //            case n: ArrayInitializerExpr =>
//            //                val (valuesCode, ctx) = gcl(n.getValues, "")
//            //                val ellip = valuesCode.map(el).mkString(", ")
//            //                rs(s"{$ellip}", ctx, valuesCode: _*)
//            //            case n: BinaryExpr =>
//            //                val (leftCode, rightCode, ctx) = gc2(n.getLeft, n.getRight, "")
//            //                if (nodes.contains(node)) (s"${el(leftCode)} ${n.getOperator.asString()} ${el(rightCode)}", ctx)
//            //                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
//            //                else (s"$leftCode$rightCode", ctx)
//            //            case n: BooleanLiteralExpr =>
//            //                if (nodes.contains(node)) (s"${n.getValue}", names)
//            //                else ("", names)
//            //            case n: CastExpr =>
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                rs(s"(${n.getType})$code", ctx, code)
//            //            case n: ClassExpr =>
//            //                if (nodes.contains(node)) (s"${n.getType}.class", names)
//            //                else ("", names)
//            //            case n: ConditionalExpr =>
//            //                val (conditionCode, thenCode, elseCode, ctx) = gc3(n.getCondition, n.getThenExpr, n.getElseExpr, "")
//            //                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
//            //            case n: ConstructorDeclaration => generateCode(n.getBody, nodes, names, indent)
//            //            case n: EnclosedExpr => generateCode(n.getInner, nodes, names, indent)
//            //            case n: FieldAccessExpr =>
//            //                val (code, ctx) = gc1(n.getScope, "")
//            //                if (nodes.contains(node)) (s"${el(code)}.${n.getName}", ctx)
//            //                else rsn(ctx, code)
//            //            case n: ForEachStmt =>
//            //                val (code, ctx) = gc1(n.getBody, s"$indent    ")
//            //                if (nodes.contains(node)) (s"${indent}for () {\n$code$indent}\n", ctx)
//            //                else rsn(ctx, code)
//            //            case n: ForStmt if n.getCompare.isEmpty =>
//            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
//            //                val (updateCode, ctx2) = gcl(n.getUpdate, "", ctx1)
//            //                val (bodyCode, ctx3) = generateCode(n.getBody, nodes, ctx2, s"$indent    ")
//            //                val codes = initCode ++ updateCode :+ bodyCode
//            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
//            //                else rsn(ctx3, codes: _*)
//            //            case n: ForStmt =>
//            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
//            //                val (compareCode, ctx2) = generateCode(n.getCompare.get(), nodes, ctx1, s"$indent    ")
//            //                val (updateCode, ctx3) = gcl(n.getUpdate, "", ctx2)
//            //                val (bodyCode, ctx4) = generateCode(n.getBody, nodes, ctx3, s"$indent    ")
//            //                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
//            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
//            //                else rsn(ctx4, codes: _*)
//            //            case n: IfStmt if n.getElseStmt.isEmpty =>
//            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
//            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
//            //                val (thenCode, ctx2) = gc1(n.getThenStmt, idt, ctx1)
//            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
//            //                else rsn(ctx2, conditionCode, thenCode)
//            //            case n: IfStmt =>
//            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
//            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
//            //                val (thenCode, elseCode, ctx2) = gc2(n.getThenStmt, n.getElseStmt.get(), idt, ctx1)
//            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
//            //                else rsn(ctx2, conditionCode, thenCode, elseCode)
//            //            case n: InstanceOfExpr =>
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                rs(s"$code instanceof ${n.getType}", ctx, code)
//            //            case n: IntegerLiteralExpr =>
//            //                rs(n.asInt().toString, names)
//            //            case n: LongLiteralExpr =>
//            //                rs(n.asLong().toString, names)
//            //            case n: MethodCallExpr if n.getScope.isEmpty =>
//            //                val (argsCode, ctx) = gcl(n.getArguments, "")
//            //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
//            //                if (nodes.contains(node)) (s"${n.getName}($ellip)", ctx)
//            //                else rsn(ctx, argsCode: _*)
//            case n: MethodDeclaration if n.getBody.isPresent =>
//                generateCodeStmt(n.getBody.get(), nodes, names, indent) match {
//                    case (Some(s), _) => block(s)
//                    case (None, _) => block()
//                }
//            //            case _: NullLiteralExpr => rs("null", names)
//            //            case n: ObjectCreationExpr=>
//            //                val (argsCode, ctx) = gcl(n.getArguments, "")
//            //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
//            //                if (nodes.contains(node)) (s"${indent}new ${n.getType}($ellip)", ctx)
//            //                else rsn(ctx, argsCode: _*)
//            //            case n: ReturnStmt if n.getExpression.isEmpty =>
//            //                rs(s"${indent}return;", names)
//            //            case n: ReturnStmt =>
//            //                val (code, ctx) = gc1(n.getExpression.get(), "")
//            //                rs(s"return $code;", ctx, code)
//            //            case n: ThrowStmt =>
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                if (nodes.contains(node)) (s"throw $code", ctx)
//            //                else rsn(ctx, code)
//            //            case _: ThisExpr => rs("this", names)
//            //            case n: UnaryExpr =>
//            //                val ope = n.getOperator
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
//            //                rs(c, ctx, code)
//            //            case n: VariableDeclarator if n.getInitializer.isEmpty =>
//            //                rs(s"${n.getType} ${n.getName}", names)
//            //            case n: VariableDeclarator =>
//            //                val name = n.getName.asString()
//            //                val (code, ctx) = gc1(n.getInitializer.get(), "")
//            //                val newCtx = if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) ctx + name else ctx
//            //                rs(s"${n.getType} $name = $code", newCtx, code)
//            //                if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) (s"${n.getType} $name = $code", ctx + name)
//            //                else rsn(ctx, code)
//            //            case n: WhileStmt =>
//            //                val (conditionCode, ctx1) = gc1(n.getCondition, "")
//            //                val (bodyCode, ctx2) = gc1(n.getBody, s"$indent    ", ctx1)
//            //                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
//            //            case n: TryStmt =>
//            //                val (code, ctx) = gc1(n.getTryBlock, indent)
//            //                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
//            //                rs(s"$indent$code", ctx, code)
//            case _ =>
//                log.warn(s"not implemented ${node.getClass} $node")
//                ???
//        }
//    }
//
//    def generateCodeStmt(node: Statement, nodes: Set[Node], names: Set[String], indent: String, noName: Boolean = false): (Option[model.Statement], Set[String]) = {
//        @inline
//        def gce1(n1: Expression, idt: String, ctx: Set[String] = names) = {
//            val (c1, ctx1) = generateCodeExpr(n1, nodes, ctx, !nodes.contains(node))
//            (c1, ctx1)
//        }
////
////        @inline
////        def gc2(n1: Node, n2: Node, idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
////            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, notVisitName)
////            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt, notVisitName)
////            (c1, c2, ctx2)
////        }
////
////        @inline
////        def gc3(n1: Node, n2: Node, n3: Node, idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
////            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt)
////            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt)
////            val (c3, ctx3) = generateCode(n3, nodes, ctx2, idt)
////            (c1, c2, c3, ctx3)
////        }
////
//        @inline
//        def gcsl[T <: Statement](ns: NodeList[T], idt: String, ctx: Set[String] = names): (Seq[model.Statement], Set[String]) = {
//            ns.asScala.foldLeft((Seq.empty[model.Statement], ctx))((context, v) => {
//                val (c, ctx) = generateCodeStmt(v, nodes, context._2, idt, !nodes.contains(node))
//                (context._1 ++ c, ctx)
//            })
//        }
//
//        @inline
//        def rs(code: => model.Node, ctx: Set[String], cs: String*): (Option[model.Node], Set[String]) = {
//            if (nodes.contains(node) || cs.exists(_ != "")) (Some(code), ctx)
//            else (None, ctx)
//        }
//
//        @inline
//        def rsn(ctx: Set[String], cs: String*) = {
//            (cs.filter(_ != "").mkString("\n"), ctx)
//        }
//
//        node match {
////            case n: AssignExpr if n.getTarget.isNameExpr =>
////                val name = n.getTarget.asNameExpr().getName.asString()
////                val (valueCode, ctx) = gc1(n.getValue, "")
////                val newCtx = if (nodes.contains(n.getValue)) ctx + name else ctx
////                rs(s"$name ${n.getOperator.asString()} $valueCode", newCtx, valueCode)
////            case n: AssignExpr =>
////                val (targetCode, valueCode, ctx) = gc2(n.getTarget, n.getValue, "")
////                rs(s"$targetCode${n.getOperator.asString()}$valueCode", ctx, targetCode, valueCode)
////            case n: ArrayAccessExpr =>
////                val (nameCode, indexCode, ctx) = gc2(n.getName, n.getIndex, "")
////                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
////            case n: ArrayCreationExpr => (s"new ${n.getElementType}[]", names)
////            case n: ArrayInitializerExpr =>
////                val (valuesCode, ctx) = gcl(n.getValues, "")
////                val ellip = valuesCode.map(el).mkString(", ")
////                rs(s"{$ellip}", ctx, valuesCode: _*)
////            case n: BinaryExpr =>
////                val (leftCode, rightCode, ctx) = gc2(n.getLeft, n.getRight, "")
////                if (nodes.contains(node)) (s"${el(leftCode)} ${n.getOperator.asString()} ${el(rightCode)}", ctx)
////                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
////                else (s"$leftCode$rightCode", ctx)
//            case n: BlockStmt =>
//                val (codes, ctx) = gcsl(n.getStatements, indent)
//                if (codes.nonEmpty) (Some(block(codes: _*)), ctx)
//                else (None, ctx)
////            case n: BooleanLiteralExpr =>
////                if (nodes.contains(node)) (s"${n.getValue}", names)
////                else ("", names)
////            case n: CastExpr =>
////                val (code, ctx) = gc1(n.getExpression, "")
////                rs(s"(${n.getType})$code", ctx, code)
////            case n: ClassExpr =>
////                if (nodes.contains(node)) (s"${n.getType}.class", names)
////                else ("", names)
////            case n: ConditionalExpr =>
////                val (conditionCode, thenCode, elseCode, ctx) = gc3(n.getCondition, n.getThenExpr, n.getElseExpr, "")
////                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
////            case n: ConstructorDeclaration => generateCode(n.getBody, nodes, names, indent)
////            case n: EnclosedExpr => generateCode(n.getInner, nodes, names, indent)
//            case n: ExpressionStmt =>
//                val (code, ctx) = gce1(n.getExpression, "")
//                code match {
//                    case Some(value) =>
//                    case None =>
//                }
//                (code.map(expr2stmt), ctx)
////            case n: FieldAccessExpr =>
////                val (code, ctx) = gc1(n.getScope, "")
////                if (nodes.contains(node)) (s"${el(code)}.${n.getName}", ctx)
////                else rsn(ctx, code)
////            case n: ForEachStmt =>
////                val (code, ctx) = gc1(n.getBody, s"$indent    ")
////                if (nodes.contains(node)) (s"${indent}for () {\n$code$indent}\n", ctx)
////                else rsn(ctx, code)
////            case n: ForStmt if n.getCompare.isEmpty =>
////                val (initCode, ctx1) = gcl(n.getInitialization, "")
////                val (updateCode, ctx2) = gcl(n.getUpdate, "", ctx1)
////                val (bodyCode, ctx3) = generateCode(n.getBody, nodes, ctx2, s"$indent    ")
////                val codes = initCode ++ updateCode :+ bodyCode
////                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
////                else rsn(ctx3, codes: _*)
////            case n: ForStmt =>
////                val (initCode, ctx1) = gcl(n.getInitialization, "")
////                val (compareCode, ctx2) = generateCode(n.getCompare.get(), nodes, ctx1, s"$indent    ")
////                val (updateCode, ctx3) = gcl(n.getUpdate, "", ctx2)
////                val (bodyCode, ctx4) = generateCode(n.getBody, nodes, ctx3, s"$indent    ")
////                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
////                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
////                else rsn(ctx4, codes: _*)
////            case n: IfStmt if n.getElseStmt.isEmpty =>
////                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
////                val idt = if (nodes.contains(node)) s"$indent    " else indent
////                val (thenCode, ctx2) = gc1(n.getThenStmt, idt, ctx1)
////                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
////                else rsn(ctx2, conditionCode, thenCode)
////            case n: IfStmt =>
////                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
////                val idt = if (nodes.contains(node)) s"$indent    " else indent
////                val (thenCode, elseCode, ctx2) = gc2(n.getThenStmt, n.getElseStmt.get(), idt, ctx1)
////                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
////                else rsn(ctx2, conditionCode, thenCode, elseCode)
////            case n: InstanceOfExpr =>
////                val (code, ctx) = gc1(n.getExpression, "")
////                rs(s"$code instanceof ${n.getType}", ctx, code)
////            case n: IntegerLiteralExpr =>
////                rs(n.asInt().toString, names)
////            case n: LongLiteralExpr =>
////                rs(n.asLong().toString, names)
////            case n: MethodCallExpr if n.getScope.isEmpty =>
////                val (argsCode, ctx) = gcl(n.getArguments, "")
////                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
////                if (nodes.contains(node)) (s"${n.getName}($ellip)", ctx)
////                else rsn(ctx, argsCode: _*)
////            case _: NullLiteralExpr => rs("null", names)
////            case n: ObjectCreationExpr=>
////                val (argsCode, ctx) = gcl(n.getArguments, "")
////                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
////                if (nodes.contains(node)) (s"${indent}new ${n.getType}($ellip)", ctx)
////                else rsn(ctx, argsCode: _*)
////            case n: ReturnStmt if n.getExpression.isEmpty =>
////                rs(s"${indent}return;", names)
////            case n: ReturnStmt =>
////                val (code, ctx) = gc1(n.getExpression.get(), "")
////                rs(s"return $code;", ctx, code)
////            case n: ThrowStmt =>
////                val (code, ctx) = gc1(n.getExpression, "")
////                if (nodes.contains(node)) (s"throw $code", ctx)
////                else rsn(ctx, code)
////            case _: ThisExpr => rs("this", names)
////            case n: UnaryExpr =>
////                val ope = n.getOperator
////                val (code, ctx) = gc1(n.getExpression, "")
////                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
////                rs(c, ctx, code)
////            case n: VariableDeclarator if n.getInitializer.isEmpty =>
////                rs(s"${n.getType} ${n.getName}", names)
////            case n: VariableDeclarator =>
////                val name = n.getName.asString()
////                val (code, ctx) = gc1(n.getInitializer.get(), "")
////                val newCtx = if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) ctx + name else ctx
////                rs(s"${n.getType} $name = $code", newCtx, code)
////                if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) (s"${n.getType} $name = $code", ctx + name)
////                else rsn(ctx, code)
////            case n: WhileStmt =>
////                val (conditionCode, ctx1) = gc1(n.getCondition, "")
////                val (bodyCode, ctx2) = gc1(n.getBody, s"$indent    ", ctx1)
////                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
////            case n: TryStmt =>
////                val (code, ctx) = gc1(n.getTryBlock, indent)
////                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
////                rs(s"$indent$code", ctx, code)
//            case _ =>
//                log.warn(s"not implemented ${node.getClass} $node")
//                (null, names)
//        }
//    }
//
//    def generateCodeExpr(node: Expression, nodes: Set[Node], names: Set[String], noName: Boolean = false): (Option[model.Expression], Set[String]) = {
//        @inline
//        def gce1(n1: Expression, ctx: Set[String] = names) = {
//            val (c1, ctx1) = generateCodeExpr(n1, nodes, ctx, !nodes.contains(node))
//            (c1, ctx1)
//        }
//        //
//        //        @inline
//        //        def gc2(n1: Node, n2: Node, idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
//        //            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt, notVisitName)
//        //            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt, notVisitName)
//        //            (c1, c2, ctx2)
//        //        }
//        //
//        //        @inline
//        //        def gc3(n1: Node, n2: Node, n3: Node, idt: String, ctx: Set[String] = names, notVisitName: Boolean = false) = {
//        //            val (c1, ctx1) = generateCode(n1, nodes, ctx, idt)
//        //            val (c2, ctx2) = generateCode(n2, nodes, ctx1, idt)
//        //            val (c3, ctx3) = generateCode(n3, nodes, ctx2, idt)
//        //            (c1, c2, c3, ctx3)
//        //        }
//        //
//        @inline
//        def gcel[T <: Expression](ns: NodeList[T], ctx: Set[String] = names): (Seq[model.Expression], Set[String]) = {
//            ns.asScala.foldLeft((Seq.empty[model.Expression], ctx))((context, v) => {
//                val (c, ctx) = generateCodeExpr(v, nodes, context._2, !nodes.contains(node))
//                (context._1 ++ c, ctx)
//            })
//        }
//
//        @inline
//        def rs(code: => model.Expression, ctx: Set[String], cs: String*): (Option[model.Expression], Set[String]) = {
//            if (nodes.contains(node) || cs.exists(_ != "")) (Some(code), ctx)
//            else (None, ctx)
//        }
//
//        @inline
//        def rsn(ctx: Set[String], cs: String*) = {
//            (cs.filter(_ != "").mkString("\n"), ctx)
//        }
//
//        node match {
//            //            case n: AssignExpr if n.getTarget.isNameExpr =>
//            //                val name = n.getTarget.asNameExpr().getName.asString()
//            //                val (valueCode, ctx) = gc1(n.getValue, "")
//            //                val newCtx = if (nodes.contains(n.getValue)) ctx + name else ctx
//            //                rs(s"$name ${n.getOperator.asString()} $valueCode", newCtx, valueCode)
//            //            case n: AssignExpr =>
//            //                val (targetCode, valueCode, ctx) = gc2(n.getTarget, n.getValue, "")
//            //                rs(s"$targetCode${n.getOperator.asString()}$valueCode", ctx, targetCode, valueCode)
//            //            case n: ArrayAccessExpr =>
//            //                val (nameCode, indexCode, ctx) = gc2(n.getName, n.getIndex, "")
//            //                rs(s"${el(nameCode)}[${el(indexCode)}]", ctx, nameCode, indexCode)
//            //            case n: ArrayCreationExpr => (s"new ${n.getElementType}[]", names)
//            //            case n: ArrayInitializerExpr =>
//            //                val (valuesCode, ctx) = gcl(n.getValues, "")
//            //                val ellip = valuesCode.map(el).mkString(", ")
//            //                rs(s"{$ellip}", ctx, valuesCode: _*)
//            //            case n: BinaryExpr =>
//            //                val (leftCode, rightCode, ctx) = gc2(n.getLeft, n.getRight, "")
//            //                if (nodes.contains(node)) (s"${el(leftCode)} ${n.getOperator.asString()} ${el(rightCode)}", ctx)
//            //                else if (leftCode != "" && rightCode != "") (s"$leftCode\n$indent$rightCode", ctx)
//            //                else (s"$leftCode$rightCode", ctx)
//            //            case n: BooleanLiteralExpr =>
//            //                if (nodes.contains(node)) (s"${n.getValue}", names)
//            //                else ("", names)
//            //            case n: CastExpr =>
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                rs(s"(${n.getType})$code", ctx, code)
//            //            case n: ClassExpr =>
//            //                if (nodes.contains(node)) (s"${n.getType}.class", names)
//            //                else ("", names)
//            //            case n: ConditionalExpr =>
//            //                val (conditionCode, thenCode, elseCode, ctx) = gc3(n.getCondition, n.getThenExpr, n.getElseExpr, "")
//            //                rs(s"$conditionCode ? $thenCode : $elseCode", ctx, conditionCode, thenCode, elseCode)
//            //            case n: ConstructorDeclaration => generateCode(n.getBody, nodes, names, indent)
//            //            case n: EnclosedExpr => generateCode(n.getInner, nodes, names, indent)
//            //            case n: FieldAccessExpr =>
//            //                val (code, ctx) = gc1(n.getScope, "")
//            //                if (nodes.contains(node)) (s"${el(code)}.${n.getName}", ctx)
//            //                else rsn(ctx, code)
//            //            case n: ForEachStmt =>
//            //                val (code, ctx) = gc1(n.getBody, s"$indent    ")
//            //                if (nodes.contains(node)) (s"${indent}for () {\n$code$indent}\n", ctx)
//            //                else rsn(ctx, code)
//            //            case n: ForStmt if n.getCompare.isEmpty =>
//            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
//            //                val (updateCode, ctx2) = gcl(n.getUpdate, "", ctx1)
//            //                val (bodyCode, ctx3) = generateCode(n.getBody, nodes, ctx2, s"$indent    ")
//            //                val codes = initCode ++ updateCode :+ bodyCode
//            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "") (s"${indent}for (${initCode.mkString("")}; ; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx3)
//            //                else rsn(ctx3, codes: _*)
//            //            case n: ForStmt =>
//            //                val (initCode, ctx1) = gcl(n.getInitialization, "")
//            //                val (compareCode, ctx2) = generateCode(n.getCompare.get(), nodes, ctx1, s"$indent    ")
//            //                val (updateCode, ctx3) = gcl(n.getUpdate, "", ctx2)
//            //                val (bodyCode, ctx4) = generateCode(n.getBody, nodes, ctx3, s"$indent    ")
//            //                val codes = (initCode :+ compareCode) ++ updateCode :+ bodyCode
//            //                if (nodes.contains(node) || initCode.mkString("") != "" || updateCode.mkString("") != "" || compareCode != "") (s"${indent}for (${initCode.mkString("")}; $compareCode; ${updateCode.mkString("")}) {\n$bodyCode$indent}\n", ctx4)
//            //                else rsn(ctx4, codes: _*)
//            //            case n: IfStmt if n.getElseStmt.isEmpty =>
//            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
//            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
//            //                val (thenCode, ctx2) = gc1(n.getThenStmt, idt, ctx1)
//            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent}\n", ctx2)
//            //                else rsn(ctx2, conditionCode, thenCode)
//            //            case n: IfStmt =>
//            //                val (conditionCode, ctx1) = generateCode(n.getCondition, nodes, names, "")
//            //                val idt = if (nodes.contains(node)) s"$indent    " else indent
//            //                val (thenCode, elseCode, ctx2) = gc2(n.getThenStmt, n.getElseStmt.get(), idt, ctx1)
//            //                if (nodes.contains(node)) (s"${indent}if (${el(conditionCode)}) {\n${el(thenCode)}$indent} else {\n${el(elseCode)}$indent}\n", ctx2)
//            //                else rsn(ctx2, conditionCode, thenCode, elseCode)
//            //            case n: InstanceOfExpr =>
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                rs(s"$code instanceof ${n.getType}", ctx, code)
//            //            case n: IntegerLiteralExpr =>
//            //                rs(n.asInt().toString, names)
//            //            case n: LongLiteralExpr =>
//            //                rs(n.asLong().toString, names)
//            //            case n: MethodCallExpr if n.getScope.isEmpty =>
//            //                val (argsCode, ctx) = gcl(n.getArguments, "")
//            //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
//            //                if (nodes.contains(node)) (s"${n.getName}($ellip)", ctx)
//            //                else rsn(ctx, argsCode: _*)
//            case n: MethodCallExpr if n.getScope.isPresent =>
//                val scope = n.getScope.get()
//                val receiverType = scope.calculateResolvedType().toString
//
//                val (argsCode, ctx1) = gcel(n.getArguments)
//                val (scopeCode, ctx2) = gce1(scope, ctx1)
//                //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
//                //                if (nodes.contains(node)) (s"$scopeCode.${n.getName}($ellip)", ctx2)
//                //                else rsn(ctx2, scopeCode +: argsCode: _*)
//                (call(scopeCode.getOrElse(HoleExpr()), receiverType, n.getName.asString(), 1), ctx2)
//            case n: NameExpr =>
//                val name = n.getName.asString()
//                if (nodes.contains(node)) (Some(name), names)
//                else if (noName) (None, names)
//                else if (names.contains(name)) (Some(name), names)
//                else (None, names)
//            //            case _: NullLiteralExpr => rs("null", names)
//            //            case n: ObjectCreationExpr=>
//            //                val (argsCode, ctx) = gcl(n.getArguments, "")
//            //                val ellip = argsCode.map(c => if (c == "") "<HOLE>" else c).mkString(", ")
//            //                if (nodes.contains(node)) (s"${indent}new ${n.getType}($ellip)", ctx)
//            //                else rsn(ctx, argsCode: _*)
//            //            case n: ReturnStmt if n.getExpression.isEmpty =>
//            //                rs(s"${indent}return;", names)
//            //            case n: ReturnStmt =>
//            //                val (code, ctx) = gc1(n.getExpression.get(), "")
//            //                rs(s"return $code;", ctx, code)
//            case n: StringLiteralExpr =>
//                rs(n.asString(), names)
//            //            case n: ThrowStmt =>
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                if (nodes.contains(node)) (s"throw $code", ctx)
//            //                else rsn(ctx, code)
//            //            case _: ThisExpr => rs("this", names)
//            //            case n: UnaryExpr =>
//            //                val ope = n.getOperator
//            //                val (code, ctx) = gc1(n.getExpression, "")
//            //                val c = if (ope.isPostfix) s"$code${ope.asString()}" else s"${ope.asString()}$code"
//            //                rs(c, ctx, code)
//            case n: VariableDeclarationExpr =>
//                generateCode(n.getVariable(0), nodes, names, "", noName)
//                log.warn(s"null here $n")
//                (null, null)
//            //            case n: VariableDeclarator if n.getInitializer.isEmpty =>
//            //                rs(s"${n.getType} ${n.getName}", names)
//            //            case n: VariableDeclarator =>
//            //                val name = n.getName.asString()
//            //                val (code, ctx) = gc1(n.getInitializer.get(), "")
//            //                val newCtx = if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) ctx + name else ctx
//            //                rs(s"${n.getType} $name = $code", newCtx, code)
//            //                if (nodes.contains(node) || nodes.contains(n.getInitializer.get())) (s"${n.getType} $name = $code", ctx + name)
//            //                else rsn(ctx, code)
//            //            case n: WhileStmt =>
//            //                val (conditionCode, ctx1) = gc1(n.getCondition, "")
//            //                val (bodyCode, ctx2) = gc1(n.getBody, s"$indent    ", ctx1)
//            //                rs(s"${indent}while ($conditionCode) {\n$bodyCode$indent}\n", ctx2, conditionCode, bodyCode)
//            //            case n: TryStmt =>
//            //                val (code, ctx) = gc1(n.getTryBlock, indent)
//            //                //                rs(s"${indent}try {\n$code$indent}\n", ctx, code)
//            //                rs(s"$indent$code", ctx, code)
//            case _ =>
//                log.warn(s"not implemented ${node.getClass} $node")
//                (null, names)
//        }
//    }
//
//}
