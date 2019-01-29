package com.github.woooking.cosyn.knowledge_graph

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.config.Config
import com.github.woooking.cosyn.qa.QuestionGenerator

object Recommendation {
    val resolver: HoleResolver = Config.holeResolver

    private val scoreOfChoice: Choice => Double = {
        case _: RecommendChoice => ???
        case _: VariableChoice => 5.0
        case _: MethodCategoryChoice => 0.0
        case _: MethodChoice => 0.0
        case _: IterableChoice => 0.0
    }

    private def penaltyOfHole(pattern: Pattern, hole: HoleExpr): Double = pattern.parentOf(hole) match {
        case statement: Statement =>
            1.0
        case expression: Expression =>
            1.0
    }

    private def recommend(context: Context, pattern: Pattern, qa: Question, hole: HoleExpr, depth: Int, score: Double, originHoles: List[HoleExpr]): List[RecommendChoice] = qa match {
        case PrimitiveQuestion(_, _) | EnumConstantQuestion(_) | StaticFieldAccessQuestion(_, _) =>
            pattern.holes diff originHoles match {
                case Nil => RecommendChoice(context, pattern, score) :: Nil
                case head :: _ => recommend(context, pattern, head, depth + 1, score, originHoles)
            }
        case ChoiceQuestion(_, choices) =>
            choices.toList.flatMap(choice => choice.action(context, pattern, hole) match {
                case NewQA(newQA) => recommend(context, pattern, newQA, hole, depth, score, originHoles)
                case Resolved(newContext, newPattern) =>
                    val newScore = score + scoreOfChoice(choice)
                    newPattern.holes diff originHoles match {
                        case Nil => RecommendChoice(newContext, newPattern, newScore) :: Nil
                        case head :: _ => recommend(newContext, newPattern, head, depth + 1, newScore, originHoles)
                    }
                case UnImplemented => Nil
            })
    }

    private def recommend(context: Context, pattern: Pattern, hole: HoleExpr, depth: Int, score: Double, originHoles: List[HoleExpr]): List[RecommendChoice] = {
        if (depth == Config.maxSearchStep) {
            val finalScore = (score /: (pattern.holes diff originHoles).map(penaltyOfHole(pattern, _))) (_ - _)
            RecommendChoice(context, pattern, finalScore) :: Nil
        } else QuestionGenerator.generateForHole(context, pattern, hole) match {
            case Some(q) => recommend(context, pattern, q, hole, depth, score, originHoles)
            case None => Nil
        }
    }

    def recommend(context: Context, pattern: Pattern, hole: HoleExpr): List[RecommendChoice] = {
        recommend(context, pattern, hole, 0, 0.0, pattern.holes)
    }
}
