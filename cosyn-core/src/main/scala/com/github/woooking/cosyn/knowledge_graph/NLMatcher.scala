package com.github.woooking.cosyn.knowledge_graph

import com.github.woooking.cosyn.entity.{MethodEntity, PatternEntity}
import com.github.woooking.cosyn.skeleton.Pattern

import scala.collection.JavaConverters._

class NLMatcher(query: String, limit: Int) {
    private val queryWords = query.split(" ").map(_.toLowerCase)

    def find(): List[(Pattern, Double)] = {
        val patterns = KnowledgeGraph.getAllPatterns
        patterns.map(evaluate).sortBy(_._2).take(limit)
    }

    private def evaluate(pattern: PatternEntity): (Pattern, Double) = {
        val methods = pattern.getHasMethods.asScala
        val methodScore = methods.map()
    }

    private def methodSim(method: MethodEntity): Double = {
        method.getSimpleName.
    }
}
