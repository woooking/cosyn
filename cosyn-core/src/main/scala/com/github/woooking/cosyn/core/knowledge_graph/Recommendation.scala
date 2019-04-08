package com.github.woooking.cosyn.core.knowledge_graph

import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.comm.skeleton.model._
import com.github.woooking.cosyn.core.qa.QuestionGenerator
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.util.TimeUtil.profile

object Recommendation {
    val resolver: HoleResolver = Config.holeResolver
    var num = 0

    private def scoreOfChoice: Choice => Double = {
        case _: RecommendChoice => ???
        case _: VariableChoice => 2.0
        case _: EnumChoice => 0.0
        case _: MethodCategoryChoice => 0.0
        case _: MethodChoice => 0.0
        case _: CreateArrayChoice => 0.0
        case IterableChoice(_, Some(_)) => 0.0 //Math.max(1.0 - (path.size - 1) * Config.iterablePenalty, 0.0)
        case IterableChoice(_, None) => 0.0 //-Config.iterablePenalty * (path.size - 1)
    }

    private def penaltyOfHole(pattern: Pattern, hole: HoleExpr): Double = pattern.parentOf(hole) match {
        case _: Statement =>
            2.0
        case _: Expression =>
            2.0
    }

    private def recommend(context: Context, qa: Question, hole: HoleExpr, depth: Int, score: Double, originHoles: List[HoleExpr], ignoredHoles: Set[HoleExpr]): List[RecommendChoice] = qa match {
        case PrimitiveQuestion(_, _) | EnumConstantQuestion(_) | StaticFieldAccessQuestion(_, _) | ArrayInitQuestion(_) =>
            val pattern = context.pattern
            pattern.holes diff originHoles diff ignoredHoles.toList match {
                case Nil => RecommendChoice(context, score) :: Nil
                case head :: _ => recommend(context, head, depth, score, originHoles, ignoredHoles + hole)
            }
        case ChoiceQuestion(_, choices) =>
            choices.toList.flatMap(choice => choice.action(context, hole) match {
                case NewQA(newQA) =>
                    recommend(context, newQA, hole, depth, score, originHoles, ignoredHoles)
                case Resolved(newContext) =>
                    val newScore = score + scoreOfChoice(choice)
                    val newPattern = newContext.pattern
                    newPattern.holes diff originHoles diff ignoredHoles.toList match {
                        case Nil =>
                            RecommendChoice(newContext, newScore) :: Nil
                        case head :: _ => recommend(newContext, head, depth + 1, newScore, originHoles, ignoredHoles)
                    }
                case UnImplemented => Nil
            })
    }

    private def recommend(context: Context, hole: HoleExpr, depth: Int, score: Double, originHoles: List[HoleExpr], ignoredHoles: Set[HoleExpr]): List[RecommendChoice] = {
        val pattern = context.pattern
        if (depth == Config.maxSearchStep) {
            num += 1
            println(num)
            val finalScore = (score /: (pattern.holes diff originHoles).map(penaltyOfHole(pattern, _))) (_ - _)
            RecommendChoice(context, finalScore) :: Nil
        } else QuestionGenerator.generateForHole(context, hole) match {
            case Some(q) => recommend(context, q, hole, depth, score, originHoles, ignoredHoles)
            case None => Nil
        }
    }

    def recommend(context: Context, hole: HoleExpr): List[RecommendChoice] = profile("recommend") {
        num = 0
        recommend(context, hole, 0, 0.0, context.pattern.holes, Set.empty[HoleExpr]).distinct
    }
}
