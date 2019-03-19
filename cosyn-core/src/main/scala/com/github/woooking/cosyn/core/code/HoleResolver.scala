package com.github.woooking.cosyn.core.code

import com.github.woooking.cosyn.comm.skeleton.model.HoleExpr
import com.github.woooking.cosyn.core.knowledge_graph.Recommendation
import com.github.woooking.cosyn.comm.skeleton.Pattern

trait HoleResolver {
    def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question]
}


