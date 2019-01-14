package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.{EnumConstantExpr, HoleExpr}

class EnumConstantHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: EnumConstantExpr =>
                Some(EnumConstantQuestion(p.enumType))
            case _ =>
                None
        }
    }

    override def recommend(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = ???
}