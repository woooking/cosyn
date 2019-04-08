package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.comm.skeleton.model.{HoleExpr, StaticFieldAccessExpr}
import com.github.woooking.cosyn.core.knowledge_graph.Recommendation
import com.github.woooking.cosyn.comm.skeleton.Pattern

class StaticFieldAccessHoleResolver extends HoleResolver {
    override def resolve(context: Context, hole: HoleExpr): Option[Question] = {
        context.pattern.parentOf(hole) match {
            case p: StaticFieldAccessExpr =>
                Some(StaticFieldAccessQuestion(p.receiverType, p.targetType))
            case _ =>
                None
        }
    }

}
