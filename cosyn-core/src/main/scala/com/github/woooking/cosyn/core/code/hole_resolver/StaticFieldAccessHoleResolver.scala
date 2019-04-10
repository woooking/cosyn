package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.comm.skeleton.model.{HoleExpr, StaticFieldAccessExpr}
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.core.recommend.Recommendation

class StaticFieldAccessHoleResolver extends HoleResolver {
    override def resolve(context: Context, hole: HoleExpr, recommend: Boolean): Option[Question] = {
        context.pattern.parentOf(hole) match {
            case p: StaticFieldAccessExpr =>
                Some(StaticFieldAccessQuestion(p.receiverType, p.targetType))
            case _ =>
                None
        }
    }

}
