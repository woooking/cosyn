package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.skeleton.Pattern
import com.github.woooking.cosyn.skeleton.model.Type.PrimitiveOrString
import com.github.woooking.cosyn.skeleton.model.{HoleExpr, MethodCallArgs, MethodCallExpr}

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
                                val entity = KnowledgeGraph.getMethodEntity(method.getQualifiedSignature)
                                val paramName = entity.getParamNames.split(",")(index)
                                val paramJavadoc = entity.getParamJavadoc(paramName)
                                Some(PrimitiveQuestion(Option(paramJavadoc), ty))
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
