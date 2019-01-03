package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.{Context, HoleResolver, Question, StaticFieldAccessQuestion}
import com.github.woooking.cosyn.code.model.expr.HoleExpr
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

class StaticFieldAccessHoleResolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[Question] = {
        hole.parent match {
            case p: StaticFieldAccessExpr =>
                Some(StaticFieldAccessQuestion(p.receiverType, p.targetType))
            case _ =>
                None
        }
    }
}
