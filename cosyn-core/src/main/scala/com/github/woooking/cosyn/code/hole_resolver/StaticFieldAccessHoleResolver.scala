package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.{HoleExpr, StaticFieldAccessExpr}

class StaticFieldAccessHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: StaticFieldAccessExpr =>
                Some(StaticFieldAccessQuestion(p.receiverType, p.targetType))
            case _ =>
                None
        }
    }

    override def recommend(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = ???
}
