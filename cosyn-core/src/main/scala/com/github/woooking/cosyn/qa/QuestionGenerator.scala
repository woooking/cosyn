package com.github.woooking.cosyn.qa

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.HoleExpr
import com.github.woooking.cosyn.config.Config
import com.github.woooking.cosyn.knowledge_graph.Recommendation

import scala.annotation.tailrec

object QuestionGenerator {
    private val resolver: HoleResolver = Config.holeResolver

    @tailrec
    private def generate(context: Context, pattern: Pattern, holes: List[HoleExpr]): Option[(HoleExpr, Question)] = {
        holes match {
            case Nil => None
            case hole :: tails =>
                resolver.resolve(context, pattern, hole) match {
                    case Some(ChoiceQuestion(question, choices)) =>
                        val recommendations = Recommendation.recommend(context, pattern, hole).sortBy(_.score)(Ordering.Double.reverse).take(3)
                        Some((hole, ChoiceQuestion(question, recommendations ++ choices)))
                    case Some(q) => Some((hole, q))
                    case None => generate(context, pattern, tails)
                }
        }
    }

    def generate(context: Context, pattern: Pattern): Option[(HoleExpr, Question)] = generate(context, pattern, pattern.holes)

    def generateForHole(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        resolver.resolve(context, pattern, hole)
    }
}
