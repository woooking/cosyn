package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.skeleton.model.{HoleExpr, StaticFieldAccessExpr}
import com.github.woooking.cosyn.knowledge_graph.Recommendation

class StaticFieldAccessHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: StaticFieldAccessExpr =>
                Some(StaticFieldAccessQuestion(p.receiverType, p.targetType))
            case _ =>
                None
        }
    }

}
