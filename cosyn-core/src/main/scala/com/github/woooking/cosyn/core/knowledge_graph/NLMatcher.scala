package com.github.woooking.cosyn.core.knowledge_graph

import com.github.woooking.cosyn.kg.entity.{MethodEntity, PatternEntity, TypeEntity}
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.google.common.base.CaseFormat
import org.json4s.native.Serialization.read

import scala.collection.JavaConverters._
import com.github.woooking.cosyn.comm.config.JsonConfig._
import com.github.woooking.cosyn.core.Components

class NLMatcher(query: String, limit: Int) {
    private val patternRepository = Components.patternRepository

    private val queryWords = query.split(" ").map(_.toLowerCase)

    def find(): List[(Pattern, Double)] = {
        val patterns = patternRepository.getAllPatterns
        patterns.map(evaluate).sortBy(_._2).take(limit)
    }

    private def evaluate(pattern: PatternEntity): (Pattern, Double) = {
        val methods = pattern.getHasMethods.asScala
        val methodScore = methods.map(methodSim).sum / methods.size
        val types = pattern.getHasTypes.asScala
        val typeScore = types.map(typeSim).sum / types.size
        val score = (methodScore + typeScore) / 2
        (read[Pattern](pattern.getPattern), score)
    }

    private def methodSim(method: MethodEntity): Double = {
        val methodWords = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, method.getSimpleName).split("_")
        if (queryWords.exists(methodWords contains)) 1.0 else 0.0
    }

    private def typeSim(ty: TypeEntity): Double = {
        val typeWords = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, ty.getSimpleName).split("_")
        if (queryWords.exists(typeWords contains)) 1.0 else 0.0
    }
}
