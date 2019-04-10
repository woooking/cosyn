package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.comm.skeleton.model.{EnumConstantExpr, HoleExpr}
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.core.recommend.Recommendation

class EnumConstantHoleResolver extends HoleResolver {
    override def resolve(context: Context, hole: HoleExpr, recommend: Boolean): Option[Question] = {
        context.pattern.parentOf(hole) match {
            case p: EnumConstantExpr =>
                Some(EnumConstantQuestion(p.enumType))
            case _ =>
                None
        }
    }

}
