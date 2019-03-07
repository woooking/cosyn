package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.skeleton.model.HoleExpr
import com.github.woooking.cosyn.knowledge_graph.Recommendation
import com.github.woooking.cosyn.skeleton.Pattern

trait HoleResolver {
    def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question]
}


