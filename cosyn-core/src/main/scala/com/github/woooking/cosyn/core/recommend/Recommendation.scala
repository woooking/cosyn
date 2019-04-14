package com.github.woooking.cosyn.core.recommend

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model._
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.core.qa.QuestionGenerator
import CodeBuilder.string2name
import com.github.woooking.cosyn.comm.skeleton.visitors.FillHoleVisitor

object Recommendation {
    val resolver: HoleResolver = Config.holeResolver

    private def scoreOfChoice: Choice => Double = {
        case _: RecommendChoice => ???
        case _: VariableChoice => 0.0
        case _: EnumChoice => -0.02
        case _: MethodCategoryChoice => ???
        case _: MethodChoice => -0.2
        case _: CreateArrayChoice => -0.2
        case IterableChoice(_, Some(_), _) => -0.2 //Math.max(1.0 - (path.size - 1) * Config.iterablePenalty, 0.0)
        case IterableChoice(_, None, _) => -0.2 //-Config.iterablePenalty * (path.size - 1)
    }

    private def penaltyOfHole(pattern: Pattern, hole: HoleExpr): Double = pattern.parentOf(hole) match {
        case _: Statement =>
            1.0
        case _: Expression =>
            1.0
    }

    private def next(context: Context, filled: Expression, depth: Int, score: Double, originHoles: List[HoleExpr], ignoredHoles: Set[HoleExpr]): List[RecommendChoice] = {
        val newPattern = context.pattern
        newPattern.holes diff originHoles diff ignoredHoles.toList match {
            case Nil => choice(context, filled, score, originHoles) :: Nil
            case head :: _ => recommend(context, Some(filled), head, depth + 1, score, originHoles, ignoredHoles)
        }
    }

    private def choice(context: Context, filled: Expression, score: Double, originHoles: List[HoleExpr]): RecommendChoice = {
        val pattern = context.pattern
        val finalScore = (score /: (pattern.holes diff originHoles).map(penaltyOfHole(pattern, _))) (_ - _)
        RecommendChoice(context, filled, finalScore)
    }

    private def recommend(context: Context, filled: Option[Expression], qa: Question, hole: HoleExpr, depth: Int, score: Double, originHoles: List[HoleExpr], ignoredHoles: Set[HoleExpr]): List[RecommendChoice] = {
        def fillExpr(expr: Expression) = filled match {
            case None => expr
            case Some(value) => FillHoleVisitor.fillHole(value, hole, expr)
        }

        qa match {
            case q @ PrimitiveQuestion(_, _) if context.pattern.parentOf(hole).isInstanceOf[MethodCallArgs] =>
                val pattern = context.pattern
                val methodArg = pattern.parentOf(hole).asInstanceOf[MethodCallArgs]
                val method = pattern.parentOf(methodArg).asInstanceOf[MethodCallExpr]
                context.nlp.recommendPrimitive(method, methodArg, q.ty).flatMap(answer => {
                    q.processInput(context, hole, answer._1) match {
                        case Question.ErrorInput(_) =>
                            ???
                        case Question.NewQuestion(_) =>
                            ???
                        case Question.Filled(newContext) =>
                            next(newContext, fillExpr(answer._1), depth, score + answer._2, originHoles, ignoredHoles)
                    }
                })
            case EnumConstantQuestion(ty) =>
                context.nlp.recommendEnum(ty).flatMap(c => qa.processInput(context, hole, c._1) match {
                    case Question.ErrorInput(_) => ???
                    case Question.NewQuestion(_) => ???
                    case Question.Filled(newContext) =>
                        next(newContext, fillExpr(c._1), depth, score - 1.0 + c._2, originHoles, ignoredHoles)
                })
            case PrimitiveQuestion(_, _) | EnumConstantQuestion(_) | StaticFieldAccessQuestion(_, _) | ArrayInitQuestion(_, _) =>
                val pattern = context.pattern
                pattern.holes diff originHoles diff ignoredHoles.toList match {
                    case Nil => filled.map(choice(context, _, score, originHoles)).toList
                    case head :: _ => recommend(context, filled, head, depth, score, originHoles, ignoredHoles + hole)
                }
            case ChoiceQuestion(_, choices) =>
                choices.toList.flatMap(c => c.action(context, hole) match {
                    case NewQA(newQA) =>
                        recommend(context, filled, newQA, hole, depth, score, originHoles, ignoredHoles)
                    case Resolved(newContext, newFilled) =>
                        next(newContext, fillExpr(newFilled), depth, score + scoreOfChoice(c), originHoles, ignoredHoles)
                    case UnImplemented => Nil
                })
        }
    }

    private def recommend(context: Context, filled: Option[Expression], hole: HoleExpr, depth: Int, score: Double, originHoles: List[HoleExpr], ignoredHoles: Set[HoleExpr]): List[RecommendChoice] = {
        if (depth == Config.maxSearchStep) {
            choice(context, filled.get, score, originHoles) :: Nil
        } else QuestionGenerator.generateForHole(context, hole) match {
            case Some(q) => recommend(context, filled, q, hole, depth, score, originHoles, ignoredHoles)
            case None => Nil
        }
    }

    def recommend(context: Context, hole: HoleExpr): List[RecommendChoice] = profile("recommend") {
        recommend(context, None, hole, 0, 0.0, context.pattern.holes, Set.empty[HoleExpr]).distinct
    }

    def recommendPrimitive(q: PrimitiveQuestion, context: Context, hole: HoleExpr): List[RecommendChoice] = profile("recommend-primitive") {
        (context, hole) match {
            case Arg(method, arg) =>
                context.nlp.recommendPrimitive(method, arg, q.ty)
                    .map(a => {
                        val newContext = q.processInput(context, hole, a._1).asInstanceOf[Question.Filled].context
                        RecommendChoice(newContext, a._1, a._2)
                    })
            case _ =>
                Nil
        }
    }

    def recommendEnum(q: EnumConstantQuestion, context: Context, hole: HoleExpr): List[RecommendChoice] = profile("recommend-enum") {
        context.nlp.recommendEnum(q.ty)
            .map(a => {
                val newContext = q.processInput(context, hole, a._1).asInstanceOf[Question.Filled].context
                RecommendChoice(newContext, a._1, a._2)
            })
    }


    object Arg {
        def unapply(ctx: (Context, HoleExpr)): Option[(MethodCallExpr, MethodCallArgs)] = {
            val context = ctx._1
            val hole = ctx._2
            if (context.pattern.parentOf(hole).isInstanceOf[MethodCallArgs]) {
                val pattern = context.pattern
                val methodArg = pattern.parentOf(hole).asInstanceOf[MethodCallArgs]
                val method = pattern.parentOf(methodArg).asInstanceOf[MethodCallExpr]
                Some((method, methodArg))
            } else {
                None
            }
        }
    }

}
