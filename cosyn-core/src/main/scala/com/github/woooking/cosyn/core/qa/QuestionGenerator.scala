package com.github.woooking.cosyn.core.qa

import com.github.woooking.cosyn.comm.skeleton.model.HoleExpr
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.core.knowledge_graph.Recommendation

import scala.annotation.tailrec

object QuestionGenerator {
    private val resolver: HoleResolver = Config.holeResolver

    @tailrec
    private def generate(context: Context, holes: List[HoleExpr]): Option[(HoleExpr, Question)] = {
        holes match {
            case Nil => None
            case hole :: tails =>
                resolver.resolve(context, hole) match {
                    case Some(ChoiceQuestion(question, choices)) =>
                        val recommendations = Recommendation.recommend(context, hole).sortBy(_.score)(Ordering.Double.reverse).take(Config.recommendNumber)
                        Some((hole, ChoiceQuestion(question, recommendations ++ choices)))
                    case Some(q) => Some((hole, q))
                    case None => generate(context, tails)
                }
        }
    }

    def generate(context: Context): Option[(HoleExpr, Question)] = generate(context, context.pattern.holes)

    def generateForHole(context: Context, hole: HoleExpr): Option[Question] = {
        resolver.resolve(context, hole)
    }
}
