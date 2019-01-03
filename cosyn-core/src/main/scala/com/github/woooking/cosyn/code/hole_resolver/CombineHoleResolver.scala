package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.model.{BlockStmt, HoleExpr}
import com.github.woooking.cosyn.code.{Context, HoleResolver, Question}

class CombineHoleResolver(resolvers: Seq[HoleResolver]) extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[Question] = {
        (Option.empty[Question] /: resolvers) ((state, resolver) => state match {
            case Some(qa) => Some(qa)
            case None => resolver.resolve(ast, hole, context)
        })
    }
}

object CombineHoleResolver {
    def apply(resolvers: HoleResolver*): CombineHoleResolver = new CombineHoleResolver(resolvers)
}
