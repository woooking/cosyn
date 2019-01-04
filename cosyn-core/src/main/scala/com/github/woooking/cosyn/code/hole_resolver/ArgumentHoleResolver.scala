package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph}
import com.github.woooking.cosyn.code.model.ty.Type.PrimitiveOrString

class ArgumentHoleResolver extends HoleResolver {
    override def resolve(pattern: Pattern, hole: HoleExpr, context: Context): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: MethodCallExpr =>
                p.args.indexWhere(_.value == hole) match {
                    case index if index != -1 =>
                        val arg = p.args(index)
                        arg.ty match {
                            case PrimitiveOrString(ty) =>
                                Some(PrimitiveQuestion(
                                    KnowledgeGraph.getMethodJavadoc(p.getQualifiedSignature).map(JavadocUtil.extractParamInfoFromJavadoc(index)),
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
