package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.{HoleExpr, MethodCallArgs, MethodCallExpr}
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph, Recommendation}
import com.github.woooking.cosyn.code.model.Type.PrimitiveOrString

class ArgumentHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: MethodCallArgs =>
                val method = pattern.parentOf(p).asInstanceOf[MethodCallExpr]
                method.args.indexWhere(_.value == hole) match {
                    case index if index != -1 =>
                        val arg = method.args(index)
                        arg.ty match {
                            case PrimitiveOrString(ty) =>
                                Some(PrimitiveQuestion(
                                    KnowledgeGraph.getMethodJavadoc(method.getQualifiedSignature).map(JavadocUtil.extractParamInfoFromJavadoc(index)),
                                    ty
                                ))
                            case _ =>
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
