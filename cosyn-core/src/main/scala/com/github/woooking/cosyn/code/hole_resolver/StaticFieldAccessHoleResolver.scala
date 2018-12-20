package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.{Context, HoleResolver, QA, StaticFieldAccessQA}
import com.github.woooking.cosyn.code.model.expr.{HoleExpr, StaticFieldAccessExpr}
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

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
