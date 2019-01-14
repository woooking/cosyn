package com.github.woooking.cosyn.qa

import com.github.woooking.cosyn.code.{Context, HoleResolver, Pattern, Question}
import com.github.woooking.cosyn.code.model.HoleExpr
import com.github.woooking.cosyn.config.Config

import scala.annotation.tailrec

object QuestionGenerator {
    val resolver: HoleResolver = Config.holeResolver

    @tailrec
    private def generate(context: Context, pattern: Pattern, holes: List[HoleExpr]): Option[(HoleExpr, Question)] = {
        holes match {
            case Nil => None
            case hole :: tails =>
                resolver.resolve(context, pattern, hole) match {
                    case Some(q) => Some((hole, q))
                    case None => generate(context, pattern, tails)
                }
        }
    }

    def generate(context: Context, pattern: Pattern): Option[(HoleExpr, Question)] = generate(context, pattern, pattern.holes)

}
