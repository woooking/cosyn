package com.github.woooking.cosyn.knowledge_graph

import com.github.woooking.cosyn.code.{Choice, Context, Pattern}
import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.config.Config

import scala.annotation.tailrec

object Recommendation {

    def recommend(context: Context, pattern: Pattern, hole: HoleExpr): List[Choice] = {
        ???
    }

    def recommend(context: Context, ty: Type): List[Choice] = {
        val methods = KnowledgeGraph.producers(context, ty)
        Nil
    }
}
