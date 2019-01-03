package com.github.woooking.cosyn.qa

import com.github.woooking.cosyn.code.{Context, Pattern, Question}
import com.github.woooking.cosyn.code.hole_resolver._
import com.github.woooking.cosyn.code.model.HoleExpr

import scala.annotation.tailrec

object QuestionGenerator {
    val resolver = CombineHoleResolver(
        new EnumConstantHoleResolver,
        new StaticFieldAccessHoleResolver,
        new ReceiverHoleResolver,
        new ArgumentHoleResolver,
        new VariableInitializationHoleResolver,
    )

    @tailrec
    private def generate(context: Context, pattern: Pattern, holes: List[HoleExpr]): Option[(HoleExpr, Question)] = {
        holes match {
            case Nil => None
            case hole :: tails =>
                resolver.resolve(pattern.stmts, hole, context) match {
                    case Some(q) => Some((hole, q))
                    case None => generate(context, pattern, tails)
                }
        }
    }

    def generate(context: Context, pattern: Pattern): Option[(HoleExpr, Question)] = generate(context, pattern, pattern.holes.toList)

}
