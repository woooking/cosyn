package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.model.{HoleExpr, VariableDeclaration}
import com.github.woooking.cosyn.code.{Context, HoleResolver, Pattern, Question}

class VariableInitializationHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: VariableDeclaration =>
                Some(QAHelper.choiceQAForType(context, p.ty))
            case _ =>
                None
        }
    }

    override def recommend(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = ???
}
