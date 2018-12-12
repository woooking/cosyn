package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, StaticFieldAccessExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern._

class StaticFieldAccessHoleResolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: StaticFieldAccessExpr =>
                Some(StaticFieldAccessQA(p.receiverType, p.targetType))
            case _ =>
                None
        }
    }
}
