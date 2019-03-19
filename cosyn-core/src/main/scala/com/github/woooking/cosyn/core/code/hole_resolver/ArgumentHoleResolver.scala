package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.Type.PrimitiveOrString
import com.github.woooking.cosyn.comm.skeleton.model.{HoleExpr, MethodCallArgs, MethodCallExpr}
import com.github.woooking.cosyn.core.Components
import com.github.woooking.cosyn.core.code._

class ArgumentHoleResolver extends HoleResolver {
    private val methodEntityRepository = Components.methodEntityRepository

    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: MethodCallArgs =>;
                val method = pattern.parentOf(p).asInstanceOf[MethodCallExpr]
                method.args.indexWhere(_.value == hole) match {
                    case index if index != -1 =>
                        val arg = method.args(index)
                        arg.ty match {
                            case PrimitiveOrString(ty) =>
                                val entity = methodEntityRepository.get(method.getQualifiedSignature)
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
