package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.javaparser.JavaParser
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph, NLP}
import com.github.woooking.cosyn.pattern._
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.util.CodeUtil


class ArgumentHoleResolver extends HoleResolver {
    private sealed trait MethodType

    private case class ConstructorType(ty: String) extends MethodType

    private case class GetType(ty: String) extends MethodType

    private case class StaticType(ty: String) extends MethodType

    private case object OtherType extends MethodType

    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: MethodCallExpr =>
                p.args.indexWhere(_.value == hole) match {
                    case index if index != -1 =>
                        val arg = p.args(index)
                        val vars = context.findVariables(arg.ty)
                        if (CodeUtil.isPrimitive(arg.ty) || arg.ty == "java.lang.String") {
                            Some(PrimitiveQA(
                                KnowledgeGraph.getMethodJavadoc(p.getQualifiedSignature).map(JavadocUtil.extractParamInfoFromJavadoc(index)),
                                arg.ty
                            ))
                        } else {
                            Some(QAHelper.choiceQAForType(context, arg.ty))
                        }
                    case _ =>
                        None
                }
            case _ =>
                None
        }
    }
}
