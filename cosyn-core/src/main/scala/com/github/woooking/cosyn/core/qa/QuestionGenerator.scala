package com.github.woooking.cosyn.core.qa

import com.github.woooking.cosyn.comm.skeleton.model.HoleExpr
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.core.recommend.Recommendation

import scala.annotation.tailrec

object QuestionGenerator {
    private val resolver: HoleResolver = Config.holeResolver

    @tailrec
    private def generate(context: Context, holes: List[HoleExpr], recommend: Boolean): Option[(HoleExpr, Question)] = {
        holes match {
            case Nil => None
            case hole :: tails =>
                resolver.resolve(context, hole, recommend) match {
                    case Some(ChoiceQuestion(question, choices)) =>
                        val recommendations = Recommendation.recommend(context, hole).sortBy(_.score)(Ordering.Double.reverse).take(Config.recommendNumber)
                        Some((hole, ChoiceQuestion(question, recommendations ++ choices)))
                    case Some(q: PrimitiveQuestion) =>
                        Some((hole, RecommendQuestion(q, Recommendation.recommendPrimitive(q, context, hole))))
                    case Some(q: EnumConstantQuestion) =>
                        Some((hole, RecommendQuestion(q, Recommendation.recommendEnum(q, context, hole))))
                    case Some(q) => Some((hole, q))
                    case None => generate(context, tails, recommend)
                }
        }
    }

    def generate(context: Context): Option[(HoleExpr, Question)] = generate(context, context.pattern.holes, recommend = false)

    def generateForHole(context: Context, hole: HoleExpr): Option[Question] = {
        resolver.resolve(context, hole, recommend = true)
    }
}
