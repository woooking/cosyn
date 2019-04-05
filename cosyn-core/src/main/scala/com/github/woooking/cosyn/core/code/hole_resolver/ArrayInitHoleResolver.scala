package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.comm.skeleton.model._
import com.github.woooking.cosyn.core.code._

class ArrayInitHoleResolver extends HoleResolver {
    override def resolve(context: Context, hole: HoleExpr): Option[Question] = {
        val pattern = context.pattern
        pattern.parentOf(hole) match {
            case p: ArrayCreationExpr if p.initializers.contains(hole) =>
                Some(ArrayInitQuestion(p.ty))
            case _ =>
                None
        }
    }
}
