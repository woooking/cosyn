package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.HoleExpr

class CombineHoleResolver(resolvers: Seq[HoleResolver]) extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        (Option.empty[Question] /: resolvers) ((state, resolver) => state match {
            case Some(qa) => Some(qa)
            case None => resolver.resolve(context, pattern, hole)
        })
    }

}

object CombineHoleResolver {
    def apply(resolvers: HoleResolver*): CombineHoleResolver = new CombineHoleResolver(resolvers)
}
