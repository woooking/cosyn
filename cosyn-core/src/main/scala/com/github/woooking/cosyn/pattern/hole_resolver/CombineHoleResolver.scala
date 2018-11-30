package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.woooking.cosyn.pattern.model.expr.HoleExpr
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern.{Context, HoleResolver, QA}

class CombineHoleResolver(resolvers: Seq[HoleResolver]) extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        (Option.empty[QA] /: resolvers) ((state, resolver) => state match {
            case Some(qa) => Some(qa)
            case None => resolver.resolve(ast, hole, context)
        })
    }
}

object CombineHoleResolver {
    def apply(resolvers: HoleResolver*): CombineHoleResolver = new CombineHoleResolver(resolvers)
}
