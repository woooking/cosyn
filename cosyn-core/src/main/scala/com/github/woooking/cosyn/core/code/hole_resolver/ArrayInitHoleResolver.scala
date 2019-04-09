package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.comm.skeleton.model.Type.PrimitiveOrString
import com.github.woooking.cosyn.comm.skeleton.model._
import com.github.woooking.cosyn.core.code._

class ArrayInitHoleResolver extends HoleResolver {
    override def resolve(context: Context, hole: HoleExpr, recommend: Boolean): Option[Question] = {
        val pattern = context.pattern
        pattern.parentOf(hole) match {
            case p: ArrayCreationExpr if p.initializers.last == hole =>
                Some(ArrayInitQuestion(p.componentType, p))
            case p: ArrayCreationExpr if p.initializers.contains(hole) =>
                p.componentType match {
                    case PrimitiveOrString(ty) =>
                        Some(PrimitiveQuestion(None, ty))
                    case ty =>
                        Some(QAHelper.choiceQAForType(context, ty, recommend))
                }
            case _ =>
                None
        }
    }
}
