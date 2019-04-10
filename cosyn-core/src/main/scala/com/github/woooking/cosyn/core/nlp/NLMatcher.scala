package com.github.woooking.cosyn.core.nlp

import com.github.woooking.cosyn.kg.entity.{MethodEntity, PatternEntity, TypeEntity}
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.core.Components
import com.google.common.base.CaseFormat

import scala.collection.JavaConverters._
import io.circe.parser.decode

class NLMatcher(query: String, limit: Int) {

    import Ordering.Double.reverse

    private val patternRepository = Components.patternRepository

    private val queryWords = query.split(" ").map(_.toLowerCase)

    def find(): List[(Pattern, Double)] = {
        val patterns = patternRepository.getAllPatterns
        patterns.map(evaluate).sortBy(_._2)(reverse).take(limit)
    }

    private def evaluate(pattern: PatternEntity): (Pattern, Double) = {
        val methods = pattern.getHasMethods.asScala
        val types = pattern.getHasTypes.asScala
        val notCoveredWords = types.foldLeft(
            methods.foldLeft(queryWords)((ws, m) => ws.filterNot(methodCover(m, _)))
        )((ws, t) => ws.filterNot(typeCover(t, _)))
        val score = 1.0 - notCoveredWords.length.toDouble / queryWords.length.toDouble
        //        val methodScore = methods.map(methodSim).sum / methods.size
        //        val typeScore = types.map(typeSim).sum / types.size
        //        val score = (methodScore + typeScore) / 2
        //        (decode[Pattern](pattern.getPattern).getOrElse(???), score)
        (decode[Pattern](pattern.getPattern).getOrElse(???), score)
    }

    private def methodCover(method: MethodEntity, word: String): Boolean = {
        val methodWords = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, method.getSimpleName).split("_")
        methodWords.contains(word)
    }

    private def typeCover(ty: TypeEntity, word: String): Boolean = {
        val typeWords = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, ty.getSimpleName).split("_")
        typeWords.contains(word)
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
