package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.skeleton.model.{HoleExpr, VariableDeclaration}
import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.knowledge_graph.Recommendation
import com.github.woooking.cosyn.skeleton.Pattern

class VariableInitializationHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: VariableDeclaration =>
                Some(QAHelper.choiceQAForType(context, p.ty))
            case _ =>
                None
        }
    }

}
