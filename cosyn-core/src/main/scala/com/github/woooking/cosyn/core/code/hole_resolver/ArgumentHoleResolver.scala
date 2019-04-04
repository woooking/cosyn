package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.Type.PrimitiveOrString
import com.github.woooking.cosyn.comm.skeleton.model._
import com.github.woooking.cosyn.core.Components
import com.github.woooking.cosyn.core.code._

class ArgumentHoleResolver extends HoleResolver {
    private val methodEntityRepository = Components.methodEntityRepository

    override def resolve(context: Context, hole: HoleExpr): Option[Question] = {
        val pattern = context.pattern
        pattern.parentOf(hole) match {
            case p: MethodCallArgs =>
                pattern.parentOf(p) match {
                    case method: MethodCallExpr =>
                        method.args.indexWhere(_.value == hole) match {
                            case index if index != -1 =>
                                val arg = method.args(index)
                                arg.ty match {
                                    case PrimitiveOrString(ty) =>
                                        val entity = methodEntityRepository.getMethod(method.getQualifiedSignature)
                                        val paramName = entity.getParamNames.split(",")(index)
                                        val paramJavadoc = entity.getParamJavadoc(paramName)
                                        Some(PrimitiveQuestion(Option(paramJavadoc), ty))
                                    case _ =>
                                        Some(QAHelper.choiceQAForType(context, arg.ty))
                                }
                            case _ =>
                                None
                        }
                    case method: ObjectCreationExpr =>
                        method.args.indexWhere(_.value == hole) match {
                            case index if index != -1 =>
                                val arg = method.args(index)
                                arg.ty match {
                                    case PrimitiveOrString(ty) =>
                                        val entity = methodEntityRepository.getMethod(method.getQualifiedSignature)
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
            case _ =>
                None
        }
    }
}
